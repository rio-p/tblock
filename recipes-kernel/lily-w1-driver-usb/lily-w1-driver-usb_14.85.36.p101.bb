#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "USB Driver/Firmware/Tools for LILY-W1 (Marvell 88W8801)"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=f35f764a6a194d7bdf2dbc7439833eb5 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=b2c0a804f5be663f0a8fa0c335b1ca19 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=5927c5d74cfe8af08015a56d6fec03bd \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=3f25958ec1ac67b5ce64079865fb9209 \
"

MRVL_PREAMBLE = "USB-UAPSTA-8801-FC18-X86"
PR = "r12"
PV = "14.85.36.p101-C3X14160"
INSTALLDIR = "/opt/lily-w1/usb8801"
MODULE_IDENTITY = "lily-w1-usb"

SRC_URI = " \
    ${LULA_DL_URL}/${MRVL_PREAMBLE}-${PV}_B0-GPL-Release.zip${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-disable-usb-suspend.patch \
    file://${PV}/0002-port-to-linux-4.10.patch \
    file://mrvl-usb8801.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://txpwrlimits/txpwrlimit_cfg_W131_TW.conf \
    file://txpwrlimits/txpwrlimit_cfg_W132_TW.conf \
"
SRC_URI[bundle.md5sum] = "b15cde240cdbe4121bed74e4a4b371a7"
SRC_URI[bundle.sha256sum] = "b8d1034bfde53858d69997b3222cd7fb1426591a01a28d08dd028e9f487eab14"

S = "${WORKDIR}/USB-8801-FC18-X86-${PV}_B0-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa

do_generate_txpower_bins_append() {
    # this might be changed for the correct module variant, but does not violate the approval if not
    ln -sf txpower_W132_TW.bin txpower_TW.bin;
}

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${WORKDIR}
    tar -xf ${MRVL_PREAMBLE}-${PV}_B0-GPL.tar
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
    oe_runmake KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${KERNEL_CC} -fgnu89-inline" LD="${KERNEL_LD}" default

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    make KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}" build -o default
}

do_install() {
    install -d ${D}/lib/firmware/mrvl
    install -m 0644 ${WORKDIR}/FwImage/usb8801_uapsta.bin ${D}/lib/firmware/mrvl/usb8801_uapsta_usb.bin

    cd ${S}/bin_usb8801
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/usb8801
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/usb8801/mlan_usb8801.ko
    install -m 0644 usb8801.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/usb8801/usb8801.ko

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
    cp -r ${WORKDIR}/mrvl-usb8801.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

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

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "lily-w1-driver"
