#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r0"
SUMMARY = "Host tools for u-blox VERA-P3"

LICENSE = "u-blox-proprietary & u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://u-blox/p3-v${PV}/COPYING;md5=b6aef854f01a4f186e0737745f213b3f \
    file://${WORKDIR}/load_vera-p3.sh;beginline=2;endline=23;md5=f260a9c3da316d35f18a260b27de810c \
"

SRC_URI = " \
    ${LULA_DL_URL}u-blox-p3_v${PV}_armv7ahf-neon.tar.gz \
    file://vera-p3.conf \
    file://load_vera-p3.sh \
"
SRC_URI[md5sum] = "92606d4d9cf17f3dcd8da4e5eb5bcf9c"
SRC_URI[sha256sum] = "c6e23e585180c692ec9e6a8e9c7fbc8aee451a04ea8d87523acfcd2ac3d958f0"

S = "${WORKDIR}"

P3_DIR = "${D}/opt/vera-p3"

RDEPENDS_${PN} += " \
    vera-p3-firmware \
    bash \
"

FILES_${PN} = "\
    /opt/vera-p3/vera-p3.conf \
    /opt/vera-p3/version.txt \
    /opt/vera-p3/bin/hif \
    /opt/vera-p3/bin/hboot \
    /opt/vera-p3/bin/hif_tool \
    /opt/vera-p3/bin/load_vera-p3.sh \
    /opt/vera-p3/setup.sh \
    /opt/vera-p3/setup-common.sh \
    /opt/vera-p3/bin/armv7l \
    /usr/lib/libubxp3.so.2 \
    /usr/lib/libubxp3.so.${PV} \
    /usr/lib/libubxp3.so \
    /opt/vera-p3/scripts/* \
"

FILES_${PN}-dev = "\
    ${includedir}/libubxp3.a \
    ${includedir}/ubxp3_api.h \
    ${includedir}/ubxp3_msgs.h \
    ${includedir}/hif_msg.h \
    ${includedir}/ieee80211.h \
    ${includedir}/gos_sae_stats.h \
    ${includedir}/hif_api_connman.h \
"

ARCH = "armv7ahf"
EXTRA_OEMAKE = 'CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}" M32="0" ARCH="${ARCH}"'

do_compile () {
    cd ${S}/u-blox/p3-v${PV}
    # Building targets in the right order according to dependencies.
    # Change this after Makefile dependencies are implemented.
    oe_runmake ubxp3
    oe_runmake ubxp3_install
    oe_runmake hif hboot
    oe_runmake install
}

do_install() {
    install -d ${P3_DIR}
    install -d ${P3_DIR}/bin
    install -m 0755 ${S}/u-blox/p3-v${PV}/bin/${ARCH}/hif ${P3_DIR}/bin
    install -m 0755 ${S}/u-blox/p3-v${PV}/bin/${ARCH}/hboot ${P3_DIR}/bin
    install -m 0755 ${S}/u-blox/p3-v${PV}/bin/${ARCH}/hif_tool ${P3_DIR}/bin
    install -m 0755 ${S}/load_vera-p3.sh ${P3_DIR}/bin
    install -m 0644 ${S}/vera-p3.conf ${P3_DIR}
    install -m 0644 ${S}/u-blox/p3-v${PV}/version.txt ${P3_DIR}/

    install -d ${D}${libdir}
    install -m 0755 ${S}/u-blox/p3-v${PV}/bin/${ARCH}/libubxp3.a ${D}${libdir}
    install -m 0755 ${S}/u-blox/p3-v${PV}/bin/${ARCH}/libubxp3.so ${D}${libdir}
    cp ${D}${libdir}/libubxp3.so ${D}${libdir}/libubxp3.so.${PV}
    cd ${D}${libdir}
    ln -s libubxp3.so.${PV} libubxp3.so.2

    install -d ${D}${includedir}
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/api/include/ubxp3_api.h ${D}${includedir}/
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/api/include/ubxp3_msgs.h ${D}${includedir}/
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/gos/hif_msg.h ${D}${includedir}/
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/gos/ieee80211.h ${D}${includedir}/
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/gos/gos_sae_stats.h ${D}${includedir}/
    install -m 0644 ${S}/u-blox/p3-v${PV}/src/host/hif/hif_api_connman.h ${D}${includedir}/

    install -d ${P3_DIR}/scripts
    install -m 0755 ${S}/u-blox/p3-v${PV}/scripts/[^b]* ${P3_DIR}/scripts/
    install -m 0755 ${S}/u-blox/p3-v${PV}/setup.sh ${P3_DIR}/
    install -m 0755 ${S}/u-blox/p3-v${PV}/setup-common.sh ${P3_DIR}/
    ln -s /opt/vera-p3/bin ${P3_DIR}/bin/armv7l
}
