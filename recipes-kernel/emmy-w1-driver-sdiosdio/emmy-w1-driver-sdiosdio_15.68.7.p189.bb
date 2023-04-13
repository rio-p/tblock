#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8887 Driver/Firmware/Tools for EMMY-W1"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://mbtc_src/README;beginline=4;endline=17;md5=0b3c87eeba2fa13d89d7795d88d36899 \
    file://mbt_src/README;beginline=4;endline=17;md5=0b3c87eeba2fa13d89d7795d88d36899 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=19;md5=ea69ae9b57618598ea13d5d729975b21 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=ea69ae9b57618598ea13d5d729975b21 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=18;md5=6914fd9f442d1ca3becc3f428ffa3785 \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=18;md5=73289dd36ca0b01e5996403488eb935d \
"

PR = "r3"
PV = "15.68.7.p189-C4X15605"
MRVL_PV = "15.68.7.p189-15.26.7.p189-C4X15605"

MODULE_NAME = "emmy-w1"
POSTFIX = "sdiosdio"
FW_VER = "15.68.7.p189"
DRIVER_VER = "C4X15605"
INSTALLDIR = "/opt/${MODULE_NAME}/sd8887-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-SD-BT-8887-U16-MMC-W${MRVL_PV}_A2-MGPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-disable-mandatory-cal_data_cfg.patch \
    file://${MRVL_PV}/0002-mlanutl-enable-PIC.patch \
    file://ed_mac_ctrl.conf \
    file://mrvl-sd8887.conf \
    file://robust_btc.conf \
    file://txpwrlimits/txpower_default.bin \
    file://txpwrlimits/txpwrlimit_cfg_161-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_165-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_KCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://txpwrlimits/txpwrlimit_cfg_SRRC.conf \
"
SRC_URI[bundle.md5sum] = "206794ff249b71c51b38847ceeb805e2"
SRC_URI[bundle.sha256sum] = "bd5ad43cb743ecd16637f2272a32800ed21215cc9d09aaa6722213046ee0df2b"

S = "${WORKDIR}/SD-UAPSTA-BT-8887-U16-MMC-V${MRVL_PV}_A2-MGPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

do_unpack[depends] = "unzip-native:do_populate_sysroot"

# since txpowerlimits class can only resolve FCC ETSI and JP
do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
    ln -f -s txpower_SRRC.bin txpower_CN.bin;
}

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd "${WORKDIR}"
    for i in *.tar; do
        tar -xf ${i}
    done

    for i in *.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="mbt_src mbtc_src wlan_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        #only build kernel modules not the apps
        KERNELDIR="${STAGING_KERNEL_BUILDDIR}" \
        module_do_compile default
    done

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    # be advised, oe_runmake has been poluted from module bbclass and should not be used for building apps.
    for i in ${dirs}; do
        cd ${S}/${i}
        # only build apps
        oe_runmake \
            KERNELDIR="${STAGING_KERNEL_BUILDDIR}" \
            CC="${CC} ${LDFLAGS} -fgnu89-inline" \
            LD="${LD}" \
            AS="${AS}" \
            build -o default
    done
}

do_install() {
    # Install firmware images
    install -d ${D}/lib/firmware/mrvl
    cd ${D}/lib/firmware/mrvl
    install -m 0644 ${WORKDIR}/FwImage/sd8887_wlan_a2.bin ./sd8887_wlan_a2_${POSTFIX}.bin
    install -m 0644 ${WORKDIR}/FwImage/sd8887_uapsta_a2.bin ./sd8887_uapsta_a2_${POSTFIX}.bin
    install -m 0644 ${WORKDIR}/FwImage/sd8887_bt_a2.bin ./sd8887_bt_a2_${POSTFIX}.bin

    # Install Wi-Fi driver
    cd ${S}/bin_sd8887
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mlan_sd8887_${POSTFIX}.ko
    install -m 0644 sd8887.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/sd8887_${POSTFIX}.ko

    # Install config files, scripts, helpers and docs
    install -d ${D}${INSTALLDIR}/
    cp -r config ${D}${INSTALLDIR}/
    cp -r wifidirect wifidisplay ${D}${INSTALLDIR}/
    cp -r README* ${D}${INSTALLDIR}/

    # Install tools and utilities
    install -m 0755 mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 mlanutl ${D}${INSTALLDIR}/
    install -m 0755 uaputl.exe ${D}${INSTALLDIR}/uaputl
    cd ${D}${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 wifidirectutl ${D}${INSTALLDIR}/

    # Install Bluetooth driver
    cd ${S}/bin_sd8887_bt
    install -m 0644 bt8887.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/bt8887_${POSTFIX}.ko
    install -m 0755 fmapp ${D}${INSTALLDIR}/
    install -m 0644 README ${D}${INSTALLDIR}/README_BT
    cp -r config ${D}${INSTALLDIR}/

    # Install char Bluetooth driver
    cd ${S}/bin_sd8887_btchar
    install -m 0644 mbt8887.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mbt8887_${POSTFIX}.ko
    install -m 0644 README ${D}${INSTALLDIR}/README_BTCHAR
    cp -r config ${D}${INSTALLDIR}/

    # Install modprobe config
    install -d ${D}${sysconfdir}/modprobe.d
    cp ${WORKDIR}/mrvl-sd8887.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # install energy detection configuration for EVK-EMMY-W1
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}${INSTALLDIR}/config/
    # install modified rbc hostcmd config file for EVK-EMMY-W1
    install -m 0644 ${WORKDIR}/robust_btc.conf ${D}${INSTALLDIR}/config/
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
