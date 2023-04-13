#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Firmware for u-blox VERA-P3"

LICENSE = "u-blox-proprietary"
LIC_FILES_CHKSUM = " \
    file://${S}/COPYING;md5=b6aef854f01a4f186e0737745f213b3f \
"

PR = "r1"

inherit allarch

S = "${WORKDIR}/u-blox/p3-v${PV}/"
FW = "firmware/u-blox/vera-p3"

SRC_URI = " \
    ${LULA_DL_URL}u-blox-p3_v${PV}_armv7ahf-neon.tar.gz \
"
SRC_URI[md5sum] = "d00a4c1a804452afb75de8d6242b13bd"
SRC_URI[sha256sum] = "d97fe5a62c0d5a233308cc713e0d4c369d8046d2a325653b887eea7468e45b54"

do_compile[noexec] = "1"

do_install() {
    # Firmware
    install -d ${D}${nonarch_base_libdir}/${FW}/
    install -m 0644 ${S}images/p3-v${PV}-52MHz.img ${D}${nonarch_base_libdir}/${FW}/vera-p3.img
    install -m 0644 ${S}images/p3-v${PV}-52MHz.img.strings ${D}${nonarch_base_libdir}/${FW}/vera-p3.img.strings
    install -m 0644 ${S}images/p3-v${PV}-52MHz-DIVERSITY.img ${D}${nonarch_base_libdir}/${FW}/vera-p3-diversity.img
    install -m 0644 ${S}images/p3-v${PV}-52MHz-DIVERSITY.img.strings ${D}${nonarch_base_libdir}/${FW}/vera-p3-diversity.img.strings

    # Board configs
    install -d ${D}${nonarch_base_libdir}/${FW}/board-configs
    install -m 0644 ${S}scripts/board-configs/* ${D}${nonarch_base_libdir}/${FW}/board-configs
}

FILES_${PN} = "\
    ${nonarch_base_libdir}/${FW}/* \
    ${nonarch_base_libdir}/${FW}/board-configs/* \
"
