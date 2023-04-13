#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "w9098 Driver/Firmware/Tools for JODY-W3"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=8f0e13ce436472d732143494812a38df \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=9a3e80eb5ab3077324e79060c898bde8 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=19f974911e4edd25d773e0ccd426962b \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=3cf02fa4277efdc47097168ee8c3c34c \
    file://uartfwloader_src/src/fw_loader_uart.h;beginline=5;endline=23;md5=ba4da79ccbbd4f6ec6e118fdc6141729 \
    file://muart_src/bt_drv.h;beginline=6;endline=19;md5=820cde90685c7fc9b46427e8de72c804 \
    file://muart_src/bt_drv.h;beginline=27;endline=44;md5=0ef5e9546509f8f0e951f5e41b142cf7 \
    file://muart_src/include/bluetooth.h;beginline=3;endline=22;md5=4452949d86d94f6a5d7efbb8651e804b \
"

PR = "r0"
PV = "17.68.0.p111-MXM4X17124"
MRVL_PV = "17.68.0.p111-17.26.0.p111-MXM4X17124_V0V1"

MODULE_NAME = "jody-w3"
POSTFIX = "pcieuart"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"
FW_VER = "17.68.0.p111"
BT_FW_VER = "17.26.0.p111"
DRIVER_VER = "MXM4X17124"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/PCIE-WLAN-UART-BT-9098-U16-X86-${MRVL_PV}-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-include-of.h-in-moal_init.c.patch \
    file://${MRVL_PV}/0002-declare-flag-EXT_HOST_MLME-from-linux-3.8.0.patch \
    file://${MRVL_PV}/0003-adapt-to-kernel-3.10.patch \
    file://jody-w3-driver-pcieuart.conf \
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

SRC_URI[bundle.md5sum] = "c97bb6015d4ed4db1b5b43578af74671"
SRC_URI[bundle.sha256sum] = "0a17ec732938e18cbc4597932b0ea2da45a214dd67f86a76624fab9229ca7df2"

S = "${WORKDIR}/PCIE-UAPSTA-UART-BT-9098-U16-X86-${MRVL_PV}-GPL"

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
