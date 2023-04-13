#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8887 Driver/Firmware/Tools for EMMY-W1"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://mbtc_src/README;beginline=4;endline=17;md5=a5b3c19ffe7061a9cc0dc18bc550ca55 \
    file://mbt_src/README;beginline=4;endline=17;md5=a5b3c19ffe7061a9cc0dc18bc550ca55 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=f35f764a6a194d7bdf2dbc7439833eb5 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=b2c0a804f5be663f0a8fa0c335b1ca19 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=5927c5d74cfe8af08015a56d6fec03bd \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=3f25958ec1ac67b5ce64079865fb9209 \
"

PR = "r14"
PV = "15.68.7.p71-C3X15146"
MRVL_PV = "15.68.7.p71-15.29.7.p71-C3X15146"

POSTFIX = "sdiosdio"
INSTALLDIR = "/opt/emmy-w1/sd8887-${POSTFIX}"
MODULE_IDENTITY = "emmy-w1-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-SD-BT-FM-NFC-8887-KK44_LINUX_3_10_33_M-PXA1908-${MRVL_PV}_A2-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-tuxify-makefiles.patch \
    file://${MRVL_PV}/0002-remove-wakelock.patch \
    file://${MRVL_PV}/0003-enable-8887-fw-dpc.patch \
    file://${MRVL_PV}/0004-enable-mfg-fw-name.patch \
    file://${MRVL_PV}/0005-uaputl-64-bit-app-with-64-bit-kernel.patch \
    file://${MRVL_PV}/0006-mlanevent-disable-buffering-stdout.patch \
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
"

SRC_URI[bundle.md5sum] = "204ce5cb69cd6144b524bf814168b822"
SRC_URI[bundle.sha256sum] = "a16d7796580637c4c81f50d777f21ea6c7dde5e63fb6f8e5e971d7b656e648b9"

S = "${WORKDIR}/SD-UAPSTA-BT-FM-8XXX-KK44_LINUX_3_10_33_M-PXA1908-${MRVL_PV}_A2-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa

do_unpack[depends] = "unzip-native:do_populate_sysroot"

# since txpowerlimits class can only resolve FCC ETSI and JP
do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
}

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd ${WORKDIR}
    unzip -o *.zip

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
    install -d ${D}/lib/firmware/mrvl
    cd ${D}/lib/firmware/mrvl
    install -m 0644 ${WORKDIR}/FwImage/sd8887_wlan_a2.bin ./sd8887_wlan_a2_${POSTFIX}.bin
    install -m 0644 ${WORKDIR}/FwImage/sd8887_uapsta_a2.bin ./sd8887_uapsta_a2_${POSTFIX}.bin
    install -m 0644 ${WORKDIR}/FwImage/sd8887_bt_a2.bin ./sd8887_bt_a2_${POSTFIX}.bin

    # Install Wi-Fi driver
    cd ${S}/bin_sd8xxx
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mlan_sd8887_${POSTFIX}.ko
    install -m 0644 sd8xxx.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/sd8887_${POSTFIX}.ko

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
    cd ${S}/bin_sd8xxx_bt
    install -m 0644 bt8xxx.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/bt8887_${POSTFIX}.ko
    install -m 0755 fmapp ${D}${INSTALLDIR}/
    install -m 0644 README ${D}${INSTALLDIR}/README_BT
    cp -r config ${D}${INSTALLDIR}/

    # Install char Bluetooth driver
    cd ${S}/bin_sd8xxx_btchar
    install -m 0644 mbt8xxx.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mbt8887_${POSTFIX}.ko
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

FILES_${PN}-firmware = "/lib/firmware/mrvl/*"
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
