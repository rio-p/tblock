#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r1"
SUMMARY = "Wireless Central Regulatory Domain Database"
HOMEPAGE = "https://www.kernel.org/pub/software/network/wireless-regdb/"

LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://LICENSE;md5=07c4f6dea3845b02a18dc00c8c87699c"

MYPV = "2016.06.10"

SRC_URI = "https://www.kernel.org/pub/software/network/wireless-regdb/wireless-regdb-${MYPV}.tar.xz;name=tarball"
SRC_URI[tarball.md5sum] = "d282cce92b6e692e8673e2bd97adf33b"
SRC_URI[tarball.sha256sum] = "cfedf1c3521b3c8f32602f25ed796e96e687c3441a00e7c050fedf7fd4f1b8b7"

inherit native

S = "${WORKDIR}/wireless-regdb-${MYPV}"

do_compile[noexec] = "1"

do_install() {
    install -d ${D}${datadir}/wireless-regdb/
    install -m 0644 ${S}/db.txt ${D}${datadir}/wireless-regdb/
}
