#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Marvell labtool for 88W8987"
DESCRIPTION = "Stand-alone labtool app for the JODY-W2 series based on the Marvell 88W8987 chipset"

# DLL/labtool version
PV1 = "1.0.0.146"
# firmware version
PV2 = "16.80.205.p146"

PR = "r1"
PV = "${PV1}-${PV2}"
INSTALLDIR = "/opt/jody-w2/mfg-tools"

LICENSE = "marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://Host/DutApiWiFiBt/DutAppVerNo.h;beginline=3;endline=4;md5=4279cf187b20e5381825657a4af65fbd \
"

DEPENDS = "bluez5"

SRC_URI = " \
    ${LULA_DL_URL}/MFG-W8987-MF-LABTOOL-U14-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
"
SRC_URI[mfgbundle.md5sum] = "5f61275316d3e47ca531d400bf4aed02"
SRC_URI[mfgbundle.sha256sum] = "a5ceddd011f8ed5c6791fafdea8b538eeeeaf9960df605a4104715dd87db5c98"

S = "${WORKDIR}/labtool_app_${PV1}"

RDEPENDS_${PN} += "jody-w2-driver"
RDEPENDS_${PN} += "jody-w2-mfg-firmware (>= ${PV})"

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
    tar -xzf MFG-W8987-MF-LABTOOL-*/bin/labtool_app_${PV1}/labtool_${PV1}-src.tgz
}

do_compile() {
    cd ${S}/Host/DutApiWiFiBt
    make -f MakeFile_W8987_FC18 BUILD_WIFI=1 BUILD_BT=1 USE_MRVL_BTSTACK=0 \
        CC="${CXX} ${LDFLAGS}" STRIP="${STRIP}"
}

do_install() {
    cd ${S}/Host/DutApiWiFiBt
    install -d ${D}${INSTALLDIR}
    install -m 0755 labtool ${D}${INSTALLDIR}/
}

INSANE_SKIP_${PN} += "already-stripped"
FILES_${PN} = "${INSTALLDIR}/*"
