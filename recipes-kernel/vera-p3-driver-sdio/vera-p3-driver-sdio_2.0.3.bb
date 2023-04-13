#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "u-blox VERA-P3 SDIO driver kernel module"

LICENSE = "u-blox-proprietary"
LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/u-blox/p3-v${PV}/COPYING;md5=b6aef854f01a4f186e0737745f213b3f \
"

PR = "r1"

S = "${WORKDIR}/u-blox/p3-v${PV}/src/p3-sdio-mod"

SRC_URI = "\
    ${LULA_DL_URL}u-blox-p3_v${PV}_armv7ahf-neon.tar.gz \
    file://vera-p3-driver-sdio.conf \
    file://0001-include-header-for-allow-signal.patch \
"
SRC_URI[md5sum] = "d00a4c1a804452afb75de8d6242b13bd"
SRC_URI[sha256sum] = "d97fe5a62c0d5a233308cc713e0d4c369d8046d2a325653b887eea7468e45b54"

inherit module deploy

RDEPENDS_${PN} += "vera-p3-firmware"

MOD_DIR = "${D}/lib/modules/${KERNEL_VERSION}/updates/u-blox/vera-p3/"
CONF_DIR = "${D}${sysconfdir}/modprobe.d/"

do_install() {
    install -d ${MOD_DIR}
    install -m 755 ${S}/p3-sdio.ko ${MOD_DIR}

    install -d ${CONF_DIR}
    install -m 0644 ${WORKDIR}/vera-p3-driver-sdio.conf ${CONF_DIR}
}

FILES_${PN} += "${sysconfdir}/modprobe.d/vera-p3-driver-sdio.conf"
