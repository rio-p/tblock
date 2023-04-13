#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools and firmware for the JODY-W3 series"
DESCRIPTION = "Manufacturing tools and firmware for the JODY-W3 series based on the Marvell 88W9098 chipset"

PR = "r1"
PV = "1.0.0.68-17.80.200.p74"
PV_A0 = "2.0.0.71-A0-17.80.200.p111"
BRIDGE_VERSION = "0.2.0.03"
MODULE_NAME = "jody-w3"
JODY_W3_UART = "/dev/ttyUSB0"

LICENSE = "marvell-confidential & u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://src_mfgbridge/bridge/mfgbridge.h;beginline=5;endline=22;md5=810f386d2f977376b26d1a8d29acdfc2 \
    file://${WORKDIR}/load-modules.sh;beginline=1;endline=22;md5=f260a9c3da316d35f18a260b27de810c \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W9098-MF-BRG-U16-WIN-X86-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    ${LULA_DL_URL}/MFG-W9098-MF-BRG-U16-WIN-X86-${PV_A0}.zip${LULA_DL_PARAMS};name=mfga0bundle \
    file://${PV}/0001-mfgbridge-enable-higher-Baud-rates-up-to-3-M.patch \
    file://load-modules.sh \
    file://unload-modules.sh \
    file://jody-w3-mfgbridge.init \
    file://jody-w3-mfgbridge.conf \
    file://jody-w3-mfg.conf \
"
SRC_URI[mfgbundle.md5sum] = "a607ae02b32b836b80bf6889cddcc906"
SRC_URI[mfgbundle.sha256sum] = "3069a89e16566f6ef4b0cd6bda376df908a164f93b7a93fc9e4a27f36b7751d3"
SRC_URI[mfga0bundle.md5sum] = "7bc6c95fb60337d104adf0bf2f7aa5f8"
SRC_URI[mfga0bundle.sha256sum] = "e318244ff06936dc8bffd0664b802e4828bbb5770954ebdf29c9b55d1fb15817"

S = "${WORKDIR}/MFG-W9098-MF-BRG-U16-WIN-X86-${PV_A0}/BRIDGE_LINUX_${BRIDGE_VERSION}"

RDEPENDS_${PN} = "jody-w3-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd "${S}"
    for i in *src.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    # Compile bridge
    cd "${S}/src_mfgbridge/bridge"
    sed -i \
        -e "s~Serial=.*~Serial=\"${JODY_UART}\"~" \
        -e "s~Load_script=.*~Load_script=\"/opt/${MODULE_NAME}/mfg-tools/load-modules.sh\"~" \
        -e "s~Unload_script=.*~Unload_script=\"/opt/${MODULE_NAME}/mfg-tools/unload-modules.sh\"~" \
        -e "s~fw=.*~fw=\"/lib/firmware/mrvl/mfg/pcieuart9098_combo.bin\"~" \
        ./bridge_init.conf
    make build CC="${CC} ${LDFLAGS}" LD="${LD}" AS="${AS}"
}

do_install() {
    cd "${WORKDIR}/MFG-W9098-MF-BRG-U16-WIN-X86-${PV}/FwImage"
    install -d ${D}/lib/firmware/mrvl/mfg

    install -m 0644 *.bin ${D}/lib/firmware/mrvl/mfg/

    # Install bridge bins
    cd "${S}/src_mfgbridge/bin_mfgbridge"
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
        -e "s~JODY_W3_UART=.*~JODY_W3_UART=\"${JODY_W3_UART}\"~" \
        ${WORKDIR}/jody-w3-mfgbridge.conf
    install -m 0755 ${WORKDIR}/jody-w3-mfgbridge.conf \
        ${D}${sysconfdir}/default/jody-w3-mfgbridge
    install -m 0755 ${WORKDIR}/jody-w3-mfgbridge.init \
        ${D}${sysconfdir}/init.d/jody-w3-mfgbridge

    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/jody-w3-mfg.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf
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
