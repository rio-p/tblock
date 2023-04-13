#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools and firmware for the JODY-W2 series"
DESCRIPTION = "Manufacturing tools and firmware for the JODY-W2 series based on the Marvell 88W8987 chipset"

PR = "r3"
PV = "1.0.0.164-16.80.205.p164"
BRIDGE_VERSION = "0.1.0.43"
MODULE_NAME = "jody-w2"
JODY_W2_UART = "/dev/ttyUSB0"

LICENSE = "marvell-confidential & u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://Bridge/bridge_linux_${BRIDGE_VERSION}/bridge/mfgbridge.h;beginline=5;endline=22;md5=810f386d2f977376b26d1a8d29acdfc2 \
    file://${WORKDIR}/load-modules.sh;beginline=1;endline=22;md5=f260a9c3da316d35f18a260b27de810c \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8987-MF-WIFI-BT-BRG-FC-VS2013-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    file://${PV}/0001-mfgbridge-enable-higher-Baud-rates-up-to-3-M.patch \
    file://${PV}/0002-mfgbridge-fix-private-ioctls-error.patch \
    file://load-modules.sh \
    file://unload-modules.sh \
    file://jody-w2-mfgbridge.init \
    file://jody-w2-mfgbridge.conf \
    file://jody-w2-mfg.conf \
"

SRC_URI[mfgbundle.md5sum] = "1d6e6c7019b70902b61282c5e787f60f"
SRC_URI[mfgbundle.sha256sum] = "2f0d7ea39a44b2eb2bea0e8b470e97cbc83e3fa41c7197713371eceba30dc1aa"

S = "${WORKDIR}/MFG-W8987-MF-WIFI-BT-BRG-FC-VS2013-${PV}"

RDEPENDS_${PN} = "jody-w2-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd "${S}/Bridge"
    for i in *src.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    # Compile bridge
    cd "${S}/Bridge/bridge_linux_${BRIDGE_VERSION}/bridge"
    sed -i \
        -e "s~Serial=.*~Serial=\"${JODY_UART}\"~" \
        -e "s~Load_script=.*~Load_script=\"/opt/${MODULE_NAME}/mfg-tools/load-modules.sh\"~" \
        -e "s~Unload_script=.*~Unload_script=\"/opt/${MODULE_NAME}/mfg-tools/unload-modules.sh\"~" \
        -e "s~fw=.*~fw=\"/lib/firmware/mrvl/mfg/sdio8987_uart_combo.bin\"~" \
        ./bridge_init.conf
    make build CC="${CC} ${LDFLAGS}" LD="${LD}" AS="${AS}"
}

do_install() {
    cd "${S}/bin/FwImage/"
    install -d ${D}/lib/firmware/mrvl/mfg

    install -m 0644 sdio8987_sdio_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 sdio8987_uart_combo.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8987d_SDIO_SDIO_SDIO.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8987d_SDIO_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8987o_SDIO_SDIO_SDIO.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8987o_SDIO_UART_UART.bin ${D}/lib/firmware/mrvl/mfg/

    # Install bridge bins
    cd "${S}/Bridge/bridge_linux_${BRIDGE_VERSION}/bin_mfgbridge"
    install -d ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 mfgbridge ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0644 bridge_init.conf ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/load-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/unload-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    cd "${S}"

    # Install init scripts
    install -d ${D}${sysconfdir}/init.d
    install -d ${D}${sysconfdir}/default
    sed -i \
        -e "s~JODY_W2_UART=.*~JODY_W2_UART=\"${JODY_W2_UART}\"~" \
        ${WORKDIR}/jody-w2-mfgbridge.conf
    install -m 0755 ${WORKDIR}/jody-w2-mfgbridge.conf \
        ${D}${sysconfdir}/default/jody-w2-mfgbridge
    install -m 0755 ${WORKDIR}/jody-w2-mfgbridge.init \
        ${D}${sysconfdir}/init.d/jody-w2-mfgbridge

    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/jody-w2-mfg.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf
}

PACKAGES += "${PN}-firmware ${PN}-bridge"

FILES_${PN}-firmware = "/lib/firmware/mrvl/mfg/*"
FILES_${PN}-bridge = "/opt/${MODULE_NAME}/mfg-tools/*"

FILES_${PN}-dbg += "/opt/${MODULE_NAME}/mfg-tools/.debug/*"

FILES_${PN} = " \
    /opt/${MODULE_NAME}/mfg-tools/license \
    /lib/firmware/mrvl/mfg/license \
    /etc/modprobe.d/* \
    /etc/init.d/* \
    /etc/default/* \
"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-bridge"
