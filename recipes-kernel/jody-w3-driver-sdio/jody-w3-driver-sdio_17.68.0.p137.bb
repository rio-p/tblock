#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "w9098 Driver/Firmware/Tools for JODY-W3"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=3cf02fa4277efdc47097168ee8c3c34c \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=19f974911e4edd25d773e0ccd426962b \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=8f0e13ce436472d732143494812a38df \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=9a3e80eb5ab3077324e79060c898bde8 \
"

PR = "r0"
PV = "17.68.0.p137-MXM4X17137"
MRVL_PV = "17.68.0.p137-17.26.0.p137-MXM4X17137"

MODULE_NAME = "jody-w3"
POSTFIX = "sdio"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"
FW_VER = "17.68.0.p137"
BT_FW_VER = "17.26.0.p137"
DRIVER_VER = "MXM4X17137"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-SD-BT-9098-U16-X86-${MRVL_PV}_V1-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-include-of.h-in-moal_init.c.patch \
    file://${MRVL_PV}/0002-declare-flag-EXT_HOST_MLME-from-linux-3.8.0.patch \
    file://jody-w3-driver-sdio.conf \
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
SRC_URI[bundle.md5sum] = "9f69f35cc11304fc6ebc5bc4782318b7"
SRC_URI[bundle.sha256sum] = "86fe425997281a0bca371fcd28e241c65fa6a6e488935234a2c7142f46d51901"

S = "${WORKDIR}/SD-UAPSTA-BT-9098-U16-X86-${MRVL_PV}_V1-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

do_unpack[depends] = "unzip-native:do_populate_sysroot"

do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
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

EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="mbt_src mbtc_src wlan_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        module_do_compile default
    done

    for i in ${dirs}; do
        cd ${S}/${i}
        # Only build apps
        oe_runmake \
            CC="${CC} ${LDFLAGS} -fgnu89-inline" \
            LD="${LD}" \
            AS="${AS}" \
            build -o default
    done
}

do_install() {
    # Install firmware images
    install -d ${D}/lib/firmware/mrvl
    local fw
    for fw in ${WORKDIR}/FwImage/*.bin; do
        install -m 0644 \
            ${fw} \
            ${D}/lib/firmware/mrvl/$(basename $(printf '%s' "${fw%.bin}_${MODULE_IDENTITY}.bin"))
    done

    # Install Wi-Fi driver
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098
    install -m 0644 \
        ${S}/bin_wlan/mlan.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098/mlan_${MODULE_IDENTITY}.ko
    install -m 0644 \
        ${S}/bin_wlan/moal.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098/moal_${MODULE_IDENTITY}.ko

    # Install Bluetooth driver
    install -d ${D}/${INSTALLDIR}/
    install -m 0644 \
        ${S}/bin_bt/bt.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098/bt9098_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_bt/fmapp ${D}/${INSTALLDIR}/
    install -m 0644 ${S}/bin_bt/README ${D}${INSTALLDIR}/README_BT

    # Install Bluetooth char driver
    install -m 0644 \
        ${S}/bin_btchar/mbt.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098/mbt9098_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_btchar/README ${D}${INSTALLDIR}/README_MBT

    # Install modprobe config
    install -d ${D}/${sysconfdir}/modprobe.d
    cp ${WORKDIR}/${PN}.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # Install tools and utilities
    install -m 0755 ${S}/bin_wlan/mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_wlan/mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 ${S}/bin_wlan/mlanutl ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_wlan/uaputl.exe ${D}${INSTALLDIR}/uaputl
    cd ${D}/${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 ${S}/bin_wlan/wifidirectutl ${D}${INSTALLDIR}/

    # Install config files, scripts, helpers and docs
    cp -r ${S}/bin_wlan/config ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_wlan/wifidirect ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_wlan/wifidisplay ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_wlan/README* ${D}/${INSTALLDIR}/

    # Install energy detection configuration for EVK-JODY-W3
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}/${INSTALLDIR}/config/
    # Install modified rbc hostcmd config file for EVK-JODY-W3
    install -m 0644 ${WORKDIR}/robust_btc.conf ${D}/${INSTALLDIR}/config/
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
RPROVIDES_${PN} += "jody-w3-driver"
