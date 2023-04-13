#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Marvell labtool for 88W8801"
DESCRIPTION = "Stand-alone labtool app for the LILY-W1 series based on the Marvell 88W8801 chipset"

# DLL/labtool version
PV1 = "2.0.0.92"
# firmware version
PV2 = "14.1.36.p59"

PR = "r3"
PV = "${PV1}-${PV2}"
INSTALLDIR = "/opt/lily-w1/mfg-tools"

LICENSE = "marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://Host/DutApiWiFiBt/DutAppVerNo.h;beginline=3;endline=4;md5=b248e1e8287753418d1c2baf5bb9a2b6 \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8801-MF-LABTOOL-FC18-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
"
SRC_URI[mfgbundle.md5sum] = "f561e22b250bae8e6ac203eaedf0443f"
SRC_URI[mfgbundle.sha256sum] = "0f66515a8414c94d74adacdbeb48a8dd2fd597d42f88a5aa09974ed9fe56e99b"

S = "${WORKDIR}/labtool_app_${PV1}"

RDEPENDS_${PN} += "lily-w1-driver"
RDEPENDS_${PN} += "lily-w1-mfg-firmware (>= ${PV})"

do_unpack_prepend() {
    dir_s = d.getVar('S', True)
    if not os.path.exists(dir_s):
        os.makedirs(dir_s)
}

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${WORKDIR}
    tar -xzf MFG-W8801-MF-LABTOOL-*/*-bin/bin/labtool_app_${PV1}/labtool_${PV1}-src.tgz
}

do_compile() {
    cd ${S}/Host/DutApiWiFiBt
    LOCAL_CXX="${@'${CXX}'.replace('-D_FORTIFY_SOURCE=2','').replace('-Werror=format-security','')}"
    LOCAL_LINK="${LOCAL_CXX}"
    make -f makefile_arm BUILD_WIFI=1 BUILD_BT=0 BUILD_NFC=0 USE_MRVL_BTSTACK=0 \
        CC="${LOCAL_CXX} ${CFLAGS}" LINK="${LOCAL_LINK} ${LDFLAGS} -L${STAGING_LIBDIR} -o labtool" STRIP="${STRIP}"
}

do_install() {
    cd ${S}/Host/DutApiWiFiBt
    install -d ${D}${INSTALLDIR}
    install -m 0755 labtool ${D}${INSTALLDIR}/
}

INSANE_SKIP_${PN} += "already-stripped"
FILES_${PN} = "${INSTALLDIR}/*"
