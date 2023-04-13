#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8987 Driver/Firmware/Tools for JODY-W2"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=3cf02fa4277efdc47097168ee8c3c34c \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=19f974911e4edd25d773e0ccd426962b \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=8f0e13ce436472d732143494812a38df \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=9a3e80eb5ab3077324e79060c898bde8 \
    file://muart_src/bt_drv.h;beginline=6;endline=19;md5=820cde90685c7fc9b46427e8de72c804 \
    file://muart_src/bt_drv.h;beginline=27;endline=44;md5=0ef5e9546509f8f0e951f5e41b142cf7 \
    file://muart_src/include/bluetooth.h;beginline=3;endline=22;md5=4452949d86d94f6a5d7efbb8651e804b \
    file://uartfwloader_src/src/fw_loader_uart.h;beginline=5;endline=23;md5=ba4da79ccbbd4f6ec6e118fdc6141729 \
"

PR = "r1"
PV = "W16.88.10.p9-C4X16636"
MRVL_PV = "W16.88.10.p9-16.26.10.p9-C4X16636"

MODULE_NAME = "jody-w2"
POSTFIX = "sdiouart"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"
FW_VER = "W16.88.10.p9"
BT_FW_VER = "16.26.10.p9"
DRIVER_VER = "C4X16636"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-UART-BT-8987-BLE_FP24-U16-MMC-${MRVL_PV}-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-uartfwloader-make-default-CC-optional.patch \
    file://jody-w2-driver-sdiouart.conf \
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
SRC_URI[bundle.md5sum] = "a9514db38af9bce93b1a3d185ab3f6da"
SRC_URI[bundle.sha256sum] = "9f224ff6b041b855b1b41c27a5c5753e2879f65b114b969bd28034f96fc8f84c"

S = "${WORKDIR}/SD-UAPSTA-UART-BT-8987-U16-MMC-${MRVL_PV}-GPL"

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
    oe_runmake make CC="${CC} ${LDFLAGS}" TARGET=W8987
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
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987
    install -m 0644 \
        ${S}/bin_sd8987/mlan.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/mlan_${MODULE_IDENTITY}.ko
    install -m 0644 \
        ${S}/bin_sd8987/sd8987.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/sd8987_${MODULE_IDENTITY}.ko

    # Install Bluetooth driver
    install -d ${D}/${INSTALLDIR}/
    install -m 0644 \
        ${S}/bin_muart/hci_uart.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/hci_uart_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_muart/README ${D}${INSTALLDIR}/README_MUART

    # Install modprobe config
    install -d ${D}/${sysconfdir}/modprobe.d
    cp ${WORKDIR}/jody-w2-driver-sdiouart.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # Install tools and utilities
    install -m 0755 ${S}/bin_sd8987/mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_sd8987/mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 ${S}/bin_sd8987/mlanutl ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_sd8987/uaputl.exe ${D}${INSTALLDIR}/uaputl
    cd ${D}/${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 ${S}/uartfwloader_src/linux/fw_loader ${D}/${INSTALLDIR}/fw_loader
    rm -rf ${S}/uartfwloader_src

    # Install config files, scripts, helpers and docs
    cp -r ${S}/bin_sd8987/config ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_sd8987/README* ${D}/${INSTALLDIR}/

    # Install energy detection configuration for EVK-JODY-W2
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}/${INSTALLDIR}/config/
    # Install modified rbc hostcmd config file for EVK-JODY-W2
    install -m 0644 ${WORKDIR}/robust_btc.conf ${D}/${INSTALLDIR}/config/
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/*"

FILES_${PN}-tools = " \
    ${INSTALLDIR}/* \
    ${INSTALLDIR}/config/* \
    ${INSTALLDIR}/wifidirect/* \
"

FILES_${PN}-dbg = "${INSTALLDIR}/.debug/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "jody-w2-driver"
