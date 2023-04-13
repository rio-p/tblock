#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r0"
SUMMARY = "Firmware for u-blox VERA-P3"

LICENSE = "u-blox-proprietary"
LIC_FILES_CHKSUM = " \
    file://COPYING;md5=c82f103c7edfae81aec6ae00ebb58f13 \
"

S = "${WORKDIR}/u-blox/p3-v${PV}/"
FW = "firmware/u-blox/vera-p3"

SRC_URI = " \
    ${LULA_DL_URL}u-blox-p3_v${PV}_armv7ahf-neon.tar.gz \
"
SRC_URI[md5sum] = "987f6b8056b8e876b5db7a6a1f29e65e"
SRC_URI[sha256sum] = "3744b64ae8bc12730b68a6aa147de2f6938543ffe261944843232e09e694bc80"

do_compile[noexec] = "1"

do_install() {
    # Firmware
    install -d ${D}${nonarch_base_libdir}/${FW}/
    install -m 0644 ${S}images/p3-v${PV}-52MHz.img ${D}${nonarch_base_libdir}/${FW}/vera-p3.img
    install -m 0644 ${S}images/p3-v${PV}-52MHz.img.strings ${D}${nonarch_base_libdir}/${FW}/vera-p3.img.strings
    install -m 0644 ${S}images/p3-v${PV}-52MHz-DIVERSITY.img ${D}${nonarch_base_libdir}/${FW}/vera-p3-diversity.img
    install -m 0644 ${S}images/p3-v${PV}-52MHz-DIVERSITY.img.strings ${D}${nonarch_base_libdir}/${FW}/vera-p3-diversity.img.strings
    install -m 0644 ${S}images/p3-v${PV}-52MHz-MFG.img ${D}${nonarch_base_libdir}/${FW}/vera-p3-mfg.img
    install -m 0644 ${S}images/p3-v${PV}-52MHz-MFG.img.strings ${D}${nonarch_base_libdir}/${FW}/vera-p3-mfg.img.strings

    # Board configs
    install -d ${D}${nonarch_base_libdir}/${FW}/board-configs
    install -m 0644 ${S}scripts/board-configs/* ${D}${nonarch_base_libdir}/${FW}/board-configs
}

FILES_${PN} = "\
    ${nonarch_base_libdir}/${FW}/* \
    ${nonarch_base_libdir}/${FW}/board-configs/* \
"
