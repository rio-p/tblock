#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools and firmware for the LILY-W1 series"
DESCRIPTION = "Manufacturing tools and firmware for the LILY-W1 series based on the marvell 88W8801 chipset"

# DLL/labtool version
PV1 = "2.0.0.92"
# firmware version
PV2 = "14.1.36.p59"
# bridge version
PV3 = "0.1.0.36"

PR = "r5"
PV = "${PV1}-${PV2}"
INSTALLDIR = "/opt/lily-w1/mfg-tools"

LICENSE = "marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://bin/bridge/bridge_linux_${PV3}/bridge/mfgbridge.h;beginline=5;endline=22;md5=7af495c660992202314e49fd61c3eb2e \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8801-MF-WIFI-BRG-FC13-WIN-X86-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    file://lily-w1-mfg.conf \
    file://lily-w1-mfgbridge \
"
SRC_URI[mfgbundle.md5sum] = "a1b8793fe800da8ca35916b1b66f4a5b"
SRC_URI[mfgbundle.sha256sum] = "12092e55322dea2343a137a135e3860d59092198df07f6e5d43bdb5cf8d5c7f2"

S = "${WORKDIR}/MFG-W8801-MF-WIFI-BRG-FC13-WIN-X86-${PV}/MFG-W8801-MF-WIFI-BRG-FC13-WIN-X86-${PV}-bin"

RDEPENDS_${PN} += "lily-w1-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${S}/bin/bridge
    tar -xzf bridge_linux_${PV3}-src.tgz
}

do_compile() {
    cd ${S}/bin/bridge/bridge_linux_${PV3}/bridge
    make CONFIG_MFG_UPDATE=y CONFIG_MARVELL_BT=n build CC="${CC} ${LDFLAGS}" LD="${LD}" AS="${AS}"
}

do_install() {
    cd ${S}/bin/FwImage/
    install -d ${D}/lib/firmware/mrvl/mfg
    install -m 0644 SDIO8801.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 USB8801.bin ${D}/lib/firmware/mrvl/mfg/

    cd ${S}/bin/bridge/bridge_linux_${PV3}/bin_mfgbridge
    install -d ${D}${INSTALLDIR}
    install -m 0755 mfgbridge ${D}${INSTALLDIR}/
    install -m 0644 bridge_init.conf ${D}${INSTALLDIR}/

    # Install modprobe config file
    install -d ${D}${sysconfdir}/modprobe.d
    install -m 0644 ${WORKDIR}/lily-w1-mfg.conf ${D}${sysconfdir}/modprobe.d/

    # Install init script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/lily-w1-mfgbridge ${D}${sysconfdir}/init.d/
}

PACKAGES += "${PN}-firmware ${PN}-bridge"

FILES_${PN}-firmware = "/lib/firmware/mrvl/mfg/*"
FILES_${PN}-bridge = "${INSTALLDIR}/*"

FILES_${PN} = "${sysconfdir}/modprobe.d/* ${sysconfdir}/init.d/*"

FILES_${PN}-dbg += "${INSTALLDIR}/.debug/*"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-bridge"
