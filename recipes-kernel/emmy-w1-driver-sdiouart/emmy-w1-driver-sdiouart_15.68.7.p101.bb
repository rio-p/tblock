#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8887 Driver/Firmware/Tools for EMMY-W1"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=f35f764a6a194d7bdf2dbc7439833eb5 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=b2c0a804f5be663f0a8fa0c335b1ca19 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=5927c5d74cfe8af08015a56d6fec03bd \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=3f25958ec1ac67b5ce64079865fb9209 \
    file://muart_src/bt_drv.h;beginline=6;endline=19;md5=20dffa4b46146167a6ad206ca6219616 \
    file://muart_src/bt_drv.h;beginline=27;endline=44;md5=6ff174ca4e46546efb9de400622901d2 \
    file://muart_src/include/bluetooth.h;beginline=2;endline=22;md5=a9368317a4d8c4a203eaba91f019469f \
"

PR = "r6"
PV = "15.68.7.p101-C3X15216"
MRVL_PV = "15.68.7.p101-15.28.7.p101-C3X15216"
#SD-WLAN-UART-BT-8887-U14-MMC-15.68.7.p101-15.28.7.p101-C3X15216_A2-GPL.zip

POSTFIX = "sdiouart"
INSTALLDIR = "/opt/emmy-w1/sd8887-${POSTFIX}"
MODULE_IDENTITY = "emmy-w1-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-UART-BT-8887-U14-MMC-${MRVL_PV}_A2-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-enable-fwname-also-for-mfgmode.patch \
    file://mrvl-sd8887.conf \
    file://ed_mac_ctrl.conf \
    file://robust_btc.conf \
    file://txpwrlimits/txpower_default.bin \
    file://txpwrlimits/txpwrlimit_cfg_161-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_165-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_KCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
"

SRC_URI[bundle.md5sum] = "3afb6e82ba76bf968952a16639159f6c"
SRC_URI[bundle.sha256sum] = "f392fb96b1d9ee451064d7e8bfe2fea34784508e7cd4efb611780e4494fd1f4e"

S = "${WORKDIR}/SD-UAPSTA-UART-BT-8887-U14-MMC-${MRVL_PV}_A2-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa

# since txpowerlimits class can only resolve FCC ETSI and JP
do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
}

# behaviour in marvell sources has been changed. Now they install not relative to the mrvl dir anymore.
# For compatibility reasons with older versions we move the installed files to /lib/firmware/${MODULE_IDENTITY}
# from lib/firmware/mrvl/${MODULE_IDENTITY}
do_install_txpower_bin_append() {
    mv -f "${D}/lib/firmware/mrvl/${MODULE_IDENTITY}" "${D}/lib/firmware/${MODULE_IDENTITY}"
}

# make sure unrar is available before unpack is starting.
# do_unpack[depends] = "unrar-native:do_populate_sysroot"
do_unpack[depends] = "unzip-native:do_populate_sysroot"
do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i
    local inter_bundle="SD-WLAN*-GPL"

    cd "${WORKDIR}"
    unzip -o *.zip

    # for i in *.rar; do
    #     unrar x -y ${i} tmpdir/
    # done

    rm -rf FwImage
    # rm -rf ${inter_bundle}
    # mv -f tmpdir/* .
    # rmdir tmpdir

    # for i in *.rar; do
    #     unrar x -y ${i}
    # done

    mv -f ${inter_bundle}/* .
    rmdir ${inter_bundle}

    for i in *.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="wlan_src muart_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        # only build kernel modules not the apps
        oe_runmake KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${KERNEL_CC}" LD="${KERNEL_LD}" default
    done

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    # be advised, oe_runmake has been poluted from module bbclass and should not be used for building apps.
    for i in ${dirs}; do
        cd ${S}/${i}
        # only build apps
        make KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}" build -o default
    done
}

do_install() {
    # Install firmware images
    install -d "${D}"/lib/firmware/mrvl/
    cd "${D}"/lib/firmware/mrvl
    install -m 0644 "${WORKDIR}"/FwImage/sd8887_wlan_a2.bin ./sd8887_wlan_a2_${POSTFIX}.bin
    install -m 0644 "${WORKDIR}"/FwImage/sduart8887_combo_a2.bin ./sd8887_uapsta_a2_${POSTFIX}.bin
    install -m 0644 "${WORKDIR}"/FwImage/uart8887_bt_a2.bin ./uart8887_bt_a2_${POSTFIX}.bin

    # Install Wi-Fi driver
    cd ${S}/bin_sd8887
    install -d "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887
    install -m 0644 mlan.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mlan_sd8887_${POSTFIX}.ko
    install -m 0644 sd8887.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/sd8887_${POSTFIX}.ko

    # Install config files, scripts, helpers and docs
    install -d "${D}"${INSTALLDIR}/
    cp -r config "${D}"${INSTALLDIR}/
    cp -r wifidirect wifidisplay "${D}"${INSTALLDIR}/
    cp -r README* "${D}"${INSTALLDIR}/

    # Install tools and utilities
    install -m 0755 mlan2040coex "${D}"${INSTALLDIR}/
    install -m 0755 mlanevent.exe "${D}"${INSTALLDIR}/mlanevent
    install -m 0755 mlanutl "${D}"${INSTALLDIR}/
    install -m 0755 uaputl.exe "${D}"${INSTALLDIR}/uaputl
    cd "${D}"${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 wifidirectutl "${D}"${INSTALLDIR}/

    # Install Bluetooth driver
    cd ${S}/bin_muart
    install -m 0644 hci_uart.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/hci_uart_${POSTFIX}.ko
    install -m 0644 README "${D}"${INSTALLDIR}/README_HCI_UART

    # Install modprobe config
    install -d "${D}"${sysconfdir}/modprobe.d
    cp -r "${WORKDIR}"/mrvl-sd8887.conf "${D}"${sysconfdir}/modprobe.d/${PN}.conf

    # install energy detection configuration for EVK-EMMY-W1
    install -m 0644 "${WORKDIR}"/ed_mac_ctrl.conf "${D}"${INSTALLDIR}/config/

    # install modified rbc hostcmd config file for EVK-EMMY-W1
    install -m 0644 "${WORKDIR}"/robust_btc.conf "${D}"${INSTALLDIR}/config/

    # install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${INSTALLDIR}/config/"
    done
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/*"

FILES_${PN}-tools = " \
    ${INSTALLDIR}/* \
    ${INSTALLDIR}/config/* \
    ${INSTALLDIR}/wifidirect/* \
    ${INSTALLDIR}/wifidisplay/* \
"

FILES_${PN}-dbg = "${INSTALLDIR}/.debug/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "emmy-w1-driver"
