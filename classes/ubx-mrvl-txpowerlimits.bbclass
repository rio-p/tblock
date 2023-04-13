#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

#
# Create native package
#
inherit ubx-mlanutl-native-provider

# Make sure the db.txt is available before this task is started
do_generate_txpower_bins[depends] += "wireless-regdb-native:do_populate_sysroot"

# Match to the right native package to provide the mlanutl for generating the txpower_xx.bin files
# Prior to 2016-06-10 causes a parse error on dizzy
DEPENDS += "wireless-regdb-native"

addtask do_generate_txpower_bins before do_install after do_compile
addtask do_patch_txpower_dir before do_configure after do_patch
addtask do_patch_txpowertbl_download_notification before do_configure after do_patch_txpower_dir
addtask do_patch_world_domain before do_configure after do_patch_txpowertbl_download_notification
addtask do_install_txpower_bin before do_package after do_install
addtask do_populate_sysroot before do_build after do_install_txpower_bin

do_patch_txpower_dir[doc] = "Update searchpath for txpower binaries to allow concurrent installation with other drivers"
do_patch_txpower_dir() {
    cd ${S}/wlan_src
    # if mlinux/moal_main.c.orig exists, move it back to achieve idempotence of this task
    mv -f mlinux/moal_main.c.orig mlinux/moal_main.c 2> /dev/null || true
    # check whether the string is still included otherwise it should fail here
    grep -e "\"txpower_XX.bin" mlinux/moal_main.c
    # backup source file
    cp -f mlinux/moal_main.c mlinux/moal_main.c.orig
    # patch searchpath for txpower_XX.bin file to /lib/firmware/mrvl/${MODULE_IDENTITY}/
    sed -i \
        -e "s|\"txpower_XX.bin|\"${MODULE_IDENTITY}/txpower_XX.bin|g" \
        mlinux/moal_main.c
}


do_patch_txpowertbl_download_notification() {
    cd ${S}/wlan_src
    # if mlinux/moal_main.c.orig exists, move it back to achieve idempotence of this task
    mv -f mlinux/moal_main.c.orig2 mlinux/moal_main.c 2> /dev/null || true

    # check whether the string is still included otherwise it should fail here.
    grep -e "\"Downloaded power table " mlinux/moal_main.c && false || true ;
    # backup source file to ext.orig2
    # append PRINTM(MMSG, "Downloaded power table (%s) to firmware\n", country);
    # patch searchpath for txpower_XX.bin file to /lib/firmware/mrvl/${MODULE_IDENTITY}/
    perl -0777 -i.orig2 \
        -pe 's/PRINTM\(MFATAL, "Download power table to firmware failed\\n"\);\n\s*ret = MLAN_STATUS_FAILURE;\n\s*}\n\s*LEAVE\(\);/PRINTM(MFATAL, "Download power table to firmware failed\\n");\n\t\tret = MLAN_STATUS_FAILURE;\n\t} else {\n\t\tPRINTM(MMSG, "Downloaded power table (%s) to firmware\\n", country);\n\t}\n\tLEAVE();/igs' \
        mlinux/moal_main.c
}


#
# Marvell does not change powertables when the destination is a so called special
# region code. World domain will be passed through.
#
do_patch_world_domain[doc] = "Assure powertable will be loaded also for world domain"
do_patch_world_domain() {
    cd ${S}/wlan_src
    local matchString='{\"00 \"}, {\"99 \"}, {\"98 \"}, {\"97 \"}'
    local filename="mlinux/moal_sta_cfg80211.c"
    # if .orig exists, move it back to achieve idempotence of this task
    mv -f "${filename}.orig" "${filename}" 2> /dev/null || true
    # check whether the string is still included otherwise it should fail here.
    grep -e "${matchString}" "${filename}"
    # backup source file
    cp -f "${filename}" "${filename}.orig"
    # remove world '00' from detection of
    sed -i \
        -e "s|${matchString}|{\"99 \"}, {\"98 \"}, {\"97 \"}|g" \
        "${filename}"
}


do_generate_txpower_bins[doc] = "Generate txpowerlimit binaries, which are downloaded to the firmware when switching between various regulatory domains"
do_generate_txpower_bins() {
    cd "${WORKDIR}"
    rm -rf gen_txpower_bin;
    mkdir gen_txpower_bin
    cd gen_txpower_bin
    local filename="";
    local domain="";
    cp -f ../txpwrlimits/* ./
    # generate binaries for the txpowerlimits
    for i in ./txpwrlimit_cfg*; do
        filename="${i##*/}"
        domain="${i##*_cfg_}"
        domain="${domain%.*}"
        echo "NOTE: gen ${filename} -> txpower_${domain}.bin"
        mlanutl-${MODULE_IDENTITY}-native mlan0 hostcmd "${filename}" generate_raw "txpower_${domain}.bin";
    done

    # link world domain to FCC
    echo "world -> FCC"
    ln -s txpower_world.bin txpower_00.bin
    ln -s txpower_FCC.bin txpower_world.bin

    local countrycode;
    local domain;

    # creating txpower_XX.bin files for various ISO 3166-1 alpha-2 country codes
    # use wireless-regdb file db.txt to map ISO 3166-1 alpha-2 information to specific regulatory domains
    IFS="
"
    for i in $(cat "${STAGING_DATADIR_NATIVE}/wireless-regdb/db.txt" | grep -E "country [A-Z]{2}:"); do
        countrycode=${i##*country }
        countrycode=${countrycode%%:*}
        domain=${i##*:}
        domain=${domain##*DFS-}
        # if country-specific txpower already exists, skip creation of the link
        if test -f txpower_${countrycode}.bin; then continue; fi

        case ${domain} in
            ETSI)
                echo "${countrycode} -> ${domain}"
                ln -s txpower_ETSI.bin txpower_${countrycode}.bin
                ;;
            FCC)
                echo "${countrycode} -> ${domain}"
                ln -s txpower_FCC.bin txpower_${countrycode}.bin
                ;;
            JP)
                echo "${countrycode} -> GITEKI"
                ln -s txpower_GITEKI.bin txpower_${countrycode}.bin
                ;;
            *)
                echo "${countrycode} -> world"
                ln -s txpower_world.bin txpower_${countrycode}.bin
                ;;
        esac
    done
    unset IFS

    #FIXME implement better sanity check
    #
    test -f txpower_DE.bin
    test -f txpower_US.bin
    test -f txpower_CA.bin
    test -f txpower_00.bin

    test -f txpower_ETSI.bin
    test -f txpower_FCC.bin
    test -f txpower_world.bin
}

do_install_txpower_bin[doc] = "Install txpower limits binaries to firmware target dir"
fakeroot do_install_txpower_bin() {
    # install txpowerlimits bin files
    # be advised: the source dir has been patched in the driver to /lib/firmware/mrvl/${MODULE_IDENTITY}/ instead of /lib/firmware/mrvl/
    install -d "${D}/lib/firmware/mrvl/${MODULE_IDENTITY}/"
    find "${WORKDIR}/gen_txpower_bin/" -name "*.bin" -type f -exec install -m 0644 {} "${D}/lib/firmware/mrvl/${MODULE_IDENTITY}/" \;
    find "${WORKDIR}/gen_txpower_bin/" -name "*.bin" -type l -exec cp --no-dereference --preserve=links {} "${D}/lib/firmware/mrvl/${MODULE_IDENTITY}/" \;
}
do_install_txpower_bin[depends] += "virtual/fakeroot-native:do_populate_sysroot"
