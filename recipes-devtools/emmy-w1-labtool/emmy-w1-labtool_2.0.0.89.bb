#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Marvell labtool for 88W8887"
DESCRIPTION = "Stand-alone labtool app for the EMMY-W1 series based on the Marvell 88W8887 chipset"

# DLL/labtool version
PV1 = "2.0.0.89"
# firmware version
PV2 = "15.2.7.p69"
#2.0.0.89-15.2.7.p69

PR = "r2"
PV = "${PV1}-${PV2}"
INSTALLDIR = "/opt/emmy-w1/mfg-tools"

LICENSE = "marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://Host/DutApiWiFiBt/DutAppVerNo.h;beginline=3;endline=4;md5=4279cf187b20e5381825657a4af65fbd \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8887-MF-LABTOOL-FC18-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    file://${PV}/0001-dont-link-static.patch \
"
SRC_URI[mfgbundle.md5sum] = "42ffa0cec6501369e0200c0be32ab15d"
SRC_URI[mfgbundle.sha256sum] = "2f12103f97c8099654b35eed7bb70e12a95d23d337fcc7be2843d75e2ee13dc8"

S = "${WORKDIR}/labtool_app_${PV1}"

RDEPENDS_${PN} += "emmy-w1-driver"
RDEPENDS_${PN} += "emmy-w1-mfg-firmware (>= ${PV})"

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${WORKDIR}
    tar -xzf MFG-W8887-MF-LABTOOL-*/*-bin/bin/labtool_app_${PV1}/labtool_${PV1}-src.tgz
}
do_compile() {
    cd ${S}/Host/DutApiWiFiBt
    LOCAL_CXX="${@'${CXX}'.replace('-D_FORTIFY_SOURCE=2','').replace('-Werror=format-security','')}"
    LOCAL_LINK="${LOCAL_CXX}"
    make -f MakeFile_8887 BUILD_WIFI=1 BUILD_BT=1 BUILD_NFC=1 USE_MRVL_BTSTACK=0 \
        CC="${LOCAL_CXX} ${CFLAGS}" LINK="${LOCAL_LINK} ${LDFLAGS} -L${STAGING_LIBDIR} -o labtool" STRIP="${STRIP}"
}

do_install() {
    cd ${S}/Host/DutApiWiFiBt
    install -d ${D}${INSTALLDIR}
    install -m 0755 labtool ${D}${INSTALLDIR}/
}

INSANE_SKIP_${PN} += "already-stripped"
FILES_${PN} = "${INSTALLDIR}/*"
