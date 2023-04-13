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
SRC_URI[md5sum] = "d36c91e6470097b4bbc5027b6732dfd6"
SRC_URI[sha256sum] = "60904ecb81e1a1d305458f3e01b8feccf20de5e97b71d60d41948b0b0f5a1d0e"

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
