#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SDIO Driver/Firmware/Tools for LILY-W1 (Marvell 88W8801)"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=f35f764a6a194d7bdf2dbc7439833eb5 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=b2c0a804f5be663f0a8fa0c335b1ca19 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=5927c5d74cfe8af08015a56d6fec03bd \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=3f25958ec1ac67b5ce64079865fb9209 \
"

MRVL_PREAMBLE = "SD-UAPSTA-8801-U14-MMC"
PR = "r6"
PV = "14.85.36.p110-C3X14215"
INSTALLDIR = "/opt/lily-w1/sd8801"
MODULE_IDENTITY = "lily-w1-sdio"

SRC_URI = " \
    ${LULA_DL_URL}/${MRVL_PREAMBLE}-${PV}_B0-GPL-Release.zip${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-fix-transposed-memset-parameters.patch \
    file://${PV}/0002-fix-woal_request_country_power_table-country_txpwrli.patch \
    file://mrvl-sd8801.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://txpwrlimits/txpwrlimit_cfg_W131_TW.conf \
    file://txpwrlimits/txpwrlimit_cfg_W132_TW.conf \
"
SRC_URI[bundle.md5sum] = "57f9a58cd15c72102e799b0817903b50"
SRC_URI[bundle.sha256sum] = "6afc4c5f40a59040ae1fb00e30d30a7797a4d9de16bfdb05ce1c63cafcdf937b"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa

do_generate_txpower_bins_append() {
    #this might be changed for the correct module variant, but does not violate the approval if not
    ln -sf txpower_W132_TW.bin txpower_TW.bin;
}

S = "${WORKDIR}/SD-8801-U14-MMC-${PV}_B0-GPL"

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local inter_bundle="${MRVL_PREAMBLE}-${PV}_B0-GPL-Release"
    local i

    cd ${WORKDIR}
    if test -d "${inter_bundle}"; then
        mv ${inter_bundle}/* .
        rmdir ${inter_bundle}
    fi
    for i in *.tar; do
        tar -xf ${i}
    done
    for i in *.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    cd ${S}/wlan_src
    # only build kernel modules not the apps
    oe_runmake KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${KERNEL_CC}" LD="${KERNEL_LD}" default

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    make KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}" build -o default
}

do_install() {
    install -d ${D}/lib/firmware/mrvl
    install -m 0644 ${WORKDIR}/FwImage/sd8801_uapsta.bin ${D}/lib/firmware/mrvl/sd8801_uapsta_sdio.bin

    cd ${S}/bin_sd8801
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8801
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8801/mlan_sd8801.ko
    install -m 0644 sd8801.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8801/sd8801.ko

    install -d ${D}${INSTALLDIR}
    install -m 0755 mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 mlanutl ${D}${INSTALLDIR}/
    install -m 0755 uaputl.exe ${D}${INSTALLDIR}/uaputl

    # copy config files
    cp -r README* config ${D}${INSTALLDIR}/

    # copy modprobe config files for prevent loading of kernel builtin
    # mwifiex modules and concurrent modules
    install -d ${D}${sysconfdir}/modprobe.d
    cp -r ${WORKDIR}/mrvl-sd8801.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${INSTALLDIR}/config/"
    done
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/mrvl/*"

FILES_${PN}-tools = " \
    ${INSTALLDIR}/* \
    ${INSTALLDIR}/config/* \
    ${INSTALLDIR}/wifidirect/* \
    ${INSTALLDIR}/wifidisplay/* \
"

FILES_${PN}-dbg = "${INSTALLDIR}/.debug/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "lily-w1-driver"
