#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r4"
DESCRIPTION = "JODY-W1 NVRAM files"

LICENSE = "cypress-proprietary"
LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/LICENSE;md5=cbc5f665d04f741f1e006d2096236ba7 \
"

SRC_URI = " \
    file://*.nvram \
"

do_install() {
    local nvram

    install -d ${D}/lib/firmware/brcm/jody-w1-pcie
    install -d ${D}/lib/firmware/brcm/jody-w1-sdio

    install -m 0644 ${WORKDIR}/${PV}/jody-w1-pcie/*.nvram ${D}/lib/firmware/brcm/jody-w1-pcie
    install -m 0644 ${WORKDIR}/${PV}/jody-w1-sdio/*.nvram ${D}/lib/firmware/brcm/jody-w1-sdio
}

FILES_${PN} = " \
    /lib/firmware/brcm/jody-w1-pcie/* \
    /lib/firmware/brcm/jody-w1-sdio/* \
"
