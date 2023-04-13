#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools and firmware for the EMMY-W1 series"
DESCRIPTION = "Manufacturing tools and firmware for the EMMY-W1 series based on the Marvell 88W8887 chipset"

PR = "r6"
PV = "2.0.0.89-15.2.7.p69"
BRIDGE_VERSION = "0.1.0.40"
MODULE_NAME = "emmy-w1"

LICENSE = "marvell-confidential & u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://bin/bridge/bridge_linux_${BRIDGE_VERSION}/bridge/mfgbridge.h;beginline=5;endline=22;md5=7af495c660992202314e49fd61c3eb2e \
    file://${WORKDIR}/load-modules.sh;beginline=1;endline=22;md5=f260a9c3da316d35f18a260b27de810c \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8887-MF-WIFI-BT-FM-BRG-FC-WIN-X86-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    file://load-modules.sh \
    file://unload-modules.sh \
    file://emmy-w1-mfgbridge \
    file://emmy-w1-mfgbridge-sdiosdio \
    file://mrvl-sd8887.conf \
"
SRC_URI[mfgbundle.md5sum] = "3a822c595c2512ed1d4d6c2f24e2576f"
SRC_URI[mfgbundle.sha256sum] = "3b309a96e77bead665796ceaa1ab1b9e9133fe8872f5d307b38efb1da4a2b2bb"

S = "${WORKDIR}/MFG-W8887-MF-WIFI-BT-FM-BRG-FC-WIN-X86-${PV}/MFG-W8887-MF-WIFI-BT-FM-BRG-FC-WIN-X86-${PV}-bin"

RDEPENDS_${PN} = "emmy-w1-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd "${S}/bin/bridge"
    for i in *src.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    # Compile bridge
    cd "${S}/bin/bridge/bridge_linux_${BRIDGE_VERSION}/bridge"
    sed -i \
        -e "s~Load_script=.*~Load_script=\"/opt/${MODULE_NAME}/mfg-tools/load-modules.sh\"~" \
        -e "s~Unload_script=.*~Unload_script=\"/opt/${MODULE_NAME}/mfg-tools/unload-modules.sh\"~" \
        -e "s~fw=.*~fw=\"/lib/firmware/mrvl/mfg/sdio8887_sdio_combo.bin\"~" \
        ./bridge_init.conf;
    make build CC="${CC} ${LDFLAGS}" LD="${LD}" AS="${AS}"
}

do_install() {
    cd "${S}/bin/FwImage/"
    install -d ${D}/lib/firmware/mrvl/mfg

    install -m 0644 pcie8887_uart_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 pcie8887_usb_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 sdio8887_sdio_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 sdio8887_uart_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 usb8887_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887d_PCIE_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887d_PCIE_USB_USB.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887d_SDIO_SDIO_SDIO.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887d_SDIO_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887d_USB_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887o_SDIO_SDIO_SDIO.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8887o_SDIO_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/

    # Install bridge binaries
    cd "${S}/bin/bridge/bridge_linux_${BRIDGE_VERSION}/bin_mfgbridge"
    install -d ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 mfgbridge ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0644 bridge_init.conf ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/load-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/unload-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    cd "${S}"

    # Install init scripts
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/emmy-w1-mfgbridge \
        ${D}${sysconfdir}/init.d/emmy-w1-mfgbridge
    install -m 0755 ${WORKDIR}/emmy-w1-mfgbridge-sdiosdio \
        ${D}${sysconfdir}/init.d/emmy-w1-mfgbridge-sdiosdio

    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/mrvl-sd8887.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf
}

PACKAGES += "${PN}-firmware ${PN}-bridge"

FILES_${PN}-firmware = "/lib/firmware/mrvl/mfg/*"
FILES_${PN}-bridge = "/opt/${MODULE_NAME}/mfg-tools/*"

FILES_${PN}-dbg += "/opt/${MODULE_NAME}/mfg-tools/.debug/*"

FILES_${PN} = "/opt/${MODULE_NAME}/mfg-tools/license /lib/firmware/mrvl/mfg/license /etc/modprobe.d/* /etc/init.d/*"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-bridge"
