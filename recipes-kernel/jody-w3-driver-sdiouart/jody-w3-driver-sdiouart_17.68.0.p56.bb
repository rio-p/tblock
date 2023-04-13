#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "w9098 Driver/Firmware/Tools for JODY-W3"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://muart_src/bt_drv.h;beginline=6;endline=19;md5=1839f52f214cfc853eff41134d749893 \
    file://muart_src/bt_drv.h;beginline=27;endline=44;md5=3cd0ac0bd0385362c1cf97baa3f90ec1 \
    file://muart_src/include/bluetooth.h;beginline=3;endline=22;md5=4452949d86d94f6a5d7efbb8651e804b \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=aebe1e8ec8e9459aeedaef9116105e64 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=ea69ae9b57618598ea13d5d729975b21 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=617b6a5d0cf1f8c94c7d49e52db6ebad \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=755fad01db1747e06175d8d01413c804 \
    file://uartfwloader_src/src/fw_loader_uart.h;beginline=5;endline=23;md5=0179d5538467ea12f65b18aea1d83083 \
"

PR = "r2"
PV = "17.68.0.p56-MX4X17045"
MRVL_PV = "17.68.0.p56-17.26.0.p56-MX4X17045"

MODULE_NAME = "jody-w3"
POSTFIX = "sdiouart"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"
FW_VER = "17.68.0.p56"
BT_FW_VER = "17.26.0.p56"
DRIVER_VER = "MX4X17045"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-UART-BT-9098-U16-X86-${MRVL_PV}-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-include-of.h-in-moal_init.c.patch \
    file://${MRVL_PV}/0002-declare-flag-EXT_HOST_MLME-from-linux-3.8.0.patch \
    file://jody-w3-driver-sdiouart.conf \
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
SRC_URI[bundle.md5sum] = "5f3852a506dc6fd8c8f21018d7d32187"
SRC_URI[bundle.sha256sum] = "ab829e65af6c7e406f0a11849e41a98bfe37a42ec13becd149e87d3e3b0922c5"

S = "${WORKDIR}/SD-UAPSTA-UART-BT-9098-U16-X86-${MRVL_PV}-GPL"

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

    local dirs="muart_src wlan_src";
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

    cd ${S}/uartfwloader_src/linux
    oe_runmake makelinks
    oe_runmake make CC="${CC} ${LDFLAGS}" TARGET=W9098
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
        ${S}/bin_muart/hci_uart.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd9098/hci_uart_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_muart/README ${D}${INSTALLDIR}/README_MUART

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
