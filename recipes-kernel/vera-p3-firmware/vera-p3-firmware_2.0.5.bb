#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r0"
SUMMARY = "Firmware for u-blox VERA-P3"

LICENSE = "u-blox-proprietary"
LIC_FILES_CHKSUM = " \
    file://COPYING;md5=b6aef854f01a4f186e0737745f213b3f \
"

S = "${WORKDIR}/u-blox/p3-v${PV}/"
FW = "firmware/u-blox/vera-p3"

SRC_URI = " \
    ${LULA_DL_URL}u-blox-p3_v${PV}_armv7ahf-neon.tar.gz \
"
SRC_URI[md5sum] = "92606d4d9cf17f3dcd8da4e5eb5bcf9c"
SRC_URI[sha256sum] = "c6e23e585180c692ec9e6a8e9c7fbc8aee451a04ea8d87523acfcd2ac3d958f0"

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
