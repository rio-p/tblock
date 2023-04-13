#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r1"
SUMMARY = "Marvell WAPI implementation"

LICENSE = "OpenSSL & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://version.h;beginline=4;endline=5;md5=196dea602264d4b2f02db23de630e7f5 \
    file://openssl-0.9.8e/LICENSE;md5=a409f902e447ddd889cffa0c70e7c7c2 \
"

PV = "1.1.0-M035"

SRC_URI = " \
    ${LULA_DL_URL}/wapi_ap_app_${PV}-src.tgz${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-fix-build.patch \
    file://wlan.conf.template \
"
SRC_URI[bundle.md5sum] = "ad2a66059b929da632c8cb8c97785a6a"
SRC_URI[bundle.sha256sum] = "51765e1fb7ad09d16f8dd6fa57227ed21adf6d207a40386e72010817f304896c"

S = "${WORKDIR}/wapi_ap_app_${PV}/wapi_ap_src"

INSTALLDIR = "/opt/wapi_ap_app"

do_compile() {
    oe_runmake \
        TARGET=ARM \
        CROSS=${TARGET_PREFIX} \
        CC="${CC} ${LDFLAGS}" \
        AR="${AR}"
}

do_install() {
    install -d ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin/uapwapi ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin/wapid ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin/ias ${D}${INSTALLDIR}/
    install -d ${D}/lib/
    install -m 0644 ${S}/lib/libcrypto.so.0.9.8e ${D}/lib/
    install -d ${D}/etc/wapi
    install -m 0644 ${WORKDIR}/wlan.conf.template ${D}/etc/wapi/
}

FILES_${PN} = " \
    ${INSTALLDIR}/* \
    /lib/* \
    /etc/wapi/* \
"

INSANE_SKIP_${PN} += "already-stripped"
