#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PV = "002.002.014.0140"
PR = "r0"
DESCRIPTION = "Patchram file for JODY-W1 Bluetooth chip"

LICENSE = "u-blox-open-source & cypress-proprietary"
LIC_FILES_CHKSUM = " \
    file://jody-w1-bt;beginline=3;endline=22;md5=08bb9ec273b70ae3ba7d783ab76946ce \
"

S = "${WORKDIR}"

SRC_URI = " \
    file://CYW89359B1_002.002.014.0140.0280.hcd \
    file://CYW89359B1_002.002.014.0140.0282_Class2.hcd \
    file://jody-w1-bt \
    file://jody-w1-bt.default \
"

do_install() {
    install -d ${D}/lib/firmware/brcm/
    install -m 0644 ${S}/CYW89359B1_002.002.014.0140.0280.hcd ${D}/lib/firmware/brcm/
    install -m 0644 ${S}/CYW89359B1_002.002.014.0140.0282_Class2.hcd ${D}/lib/firmware/brcm/
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/jody-w1-bt ${D}${sysconfdir}/init.d/
    install -d ${D}${sysconfdir}/default
    install -m 0644 ${S}/jody-w1-bt.default ${D}${sysconfdir}/default/jody-w1-bt
}

FILES_${PN} = " \
    /lib/firmware/brcm/CYW89359B1_002.002.014.0140.0280.hcd \
    /lib/firmware/brcm/CYW89359B1_002.002.014.0140.0282_Class2.hcd \
    ${sysconfdir}/init.d/jody-w1-bt \
    ${sysconfdir}/default/jody-w1-bt \
"

RDEPENDS_${PN} += "brcm-patchram-plus"
