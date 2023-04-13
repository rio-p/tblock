#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

#
# https://code.google.com/archive/p/broadcom-bluetooth/
#

PR = "r1"
SUMMARY = "Broadcom Bluetooth chip Support on Linux"
DESCRIPTION = "Utility to configure and test Broadcom Bluetooth chips on Linux"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://brcm_patchram_plus.c;beginline=1;endline=17;md5=691691b063f1b4034300dc452e36b68d"

SRC_URI = "file://brcm_patchram_plus.c"

S = "${WORKDIR}"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -o brcm_patchram_plus brcm_patchram_plus.c
}

do_install() {
    install -d ${D}${bindir}/
    install -m 0755 ${S}/brcm_patchram_plus ${D}${bindir}/
}

FILES_${PN} = "${bindir}/brcm_patchram_plus"
