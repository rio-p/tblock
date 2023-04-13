#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools for the JODY-W1 series"
DESCRIPTION = "Manufacturing tools for the JODY-W1 series based on the BCM89359 chipset"

PR = "r3"
PV = "20161020"
MODULE_NAME = "jody-w1"

LICENSE = "CLOSED"

SRC_URI = " \
    ${LULA_DL_URL}/wl_and_dhd_utility_${PV}.7z${LULA_DL_PARAMS};name=bundle \
    file://jody-w1-mfg.conf \
"

SRC_URI[bundle.md5sum] = "fb1ebe61f5635cd7cd3a2b5503619173"
SRC_URI[bundle.sha256sum] = "6987131cdf5ee8e855c399acb9846d8b23d9de9e32c9c42fdf04f381635af497"

S = "${WORKDIR}/wl_and_dhd_utility"
do_unpack[depends] += "p7zip-native:do_populate_sysroot"

RDEPENDS_${PN} = "jody-w1-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${WORKDIR}
    if test -f wl_and_dhd_utility_${PV}.7z; then
        7z x -y wl_and_dhd_utility_${PV}.7z
    fi
}

do_install() {
    install -d ${D}/opt/${MODULE_NAME}/mfg-tools
    install -m 0755 ${S}/wl ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${S}/dhd ${D}/opt/${MODULE_NAME}/mfg-tools/

    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/jody-w1-mfg.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf
}

# Make arm32 binaries as "allarch". We can run it for arm64 using multilib.  
INSANE_SKIP = "arch"

FILES_${PN} = "/opt/${MODULE_NAME}/mfg-tools/* /etc/modprobe.d/*"

FILES_${PN}-dbg += "/opt/${MODULE_NAME}/mfg-tools/.debug/*"

RDEPENDS_${PN} += "${PN}"
RDEPENDS_${PN} += "${@'lib32-libgcc' if d.getVar('TARGET_ARCH', True) == ('aarch64') else ''}"

RPROVIDES_${PN} += "${PN}"
