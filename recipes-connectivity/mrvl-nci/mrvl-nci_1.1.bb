#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r4"
SUMMARY = "Proprietary NFC stack from Marvell"

LICENSE = "marvell-confidential"
LIC_FILES_CHKSUM = "file://include/mrvl_nci.h;beginline=6;endline=20;md5=ec78032ecb47b15e47b13718f91a3dd8"

PROVIDES = "mrvl-nci"

SRC_URI = " \
    ${LULA_DL_URL}/mrvl_nci-${PV}.tar.bz2${LULA_DL_PARAMS};name=tarball \
    file://${PV}/0001-use-sdio-interface.patch \
"
SRC_URI[tarball.md5sum] = "5cfd95bec8aa79f3b65ec0b57d21c569"
SRC_URI[tarball.sha256sum] = "d7d956432c10c24273dd65ba38d3df6a792037e88562defeecb3e840feebe8aa"

S = "${WORKDIR}/mrvl_nci-${PV}"

do_compile() {
    cd "${S}";
    make CC="$CC $LDFLAGS" all
}

do_install() {
    install -d ${D}/${libdir}
    install -m 0644 ${S}/libmrvl_nci.a ${D}/${libdir}/

    install -d ${D}/${bindir}
    install -m 0755 ${S}/demo/demo_bt_oob ${D}/${bindir}/${PN}_demo_bt_oob
    install -m 0755 ${S}/demo/demo_hce ${D}/${bindir}/${PN}_demo_hce
    install -m 0755 ${S}/demo/demo_ndef ${D}/${bindir}/${PN}_demo_ndef
    install -m 0755 ${S}/demo/demo_ndef_decoder ${D}/${bindir}/${PN}_demo_ndef_decoder
    install -m 0755 ${S}/demo/demo_ndef_encoder ${D}/${bindir}/${PN}_demo_ndef_encoder
    install -m 0755 ${S}/demo/demo_p2p ${D}/${bindir}/${PN}_demo_p2p
    install -m 0755 ${S}/demo/demo_reader ${D}/${bindir}/${PN}_demo_reader
    install -m 0755 ${S}/demo/demo_text ${D}/${bindir}/${PN}_demo_text

    install -d ${D}/${includedir}
    install -m 0644 ${S}/include/mrvl_nci.h  ${D}/${includedir}/
    install -m 0644 ${S}/include/mrvl_nci_defines.h  ${D}/${includedir}/

    install -d ${D}/${docdir}/${PN}/
    install -m 0644 ${S}/doc/"Marvell NFC - MRVL NCI - Customer.doc"  ${D}/${docdir}/${PN}/
}

PACKAGES += "${PN}-lib"

# dependency to shared lib has been suspended.
#RDEPENDS_${PN} += "${PN}-lib ${PN}-doc"
RDEPENDS_${PN} += "${PN}-doc"

FILES_${PN}-lib = "${libdir}/*"
FILES_${PN}-doc = "${docdir}/*"
FILES_${PN}-dev = "${includedir}"
FILES_${PN}-dbg = " \
    ${libdir}/.debug/* \
    ${bindir}/.debug/* \
    /usr/src/debug \
"
