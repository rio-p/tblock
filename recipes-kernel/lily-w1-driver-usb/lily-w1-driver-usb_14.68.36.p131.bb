#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "USB Driver/Firmware/Tools for LILY-W1 (Marvell 88W8801)"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=19;md5=ea69ae9b57618598ea13d5d729975b21 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=ea69ae9b57618598ea13d5d729975b21 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=18;md5=6914fd9f442d1ca3becc3f428ffa3785 \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=18;md5=73289dd36ca0b01e5996403488eb935d \
"

MRVL_PREAMBLE="USB-UAPSTA-8801-U16-X86"
PR = "r4"
PV = "14.68.36.p131-C4X14616"

MODULE_NAME = "lily-w1"
POSTFIX = "usb"
FW_VER = "14.68.36.p131"
DRIVER_VER = "C4X14616"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}8801"

SRC_URI = " \
    ${LULA_DL_URL}/${MRVL_PREAMBLE}-W${PV}_B0-MGPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-remove-offending-inlines.patch \
    file://lily-w1.rules \
    file://mrvl-usb8801.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://txpwrlimits/txpwrlimit_cfg_W131_TW.conf \
    file://txpwrlimits/txpwrlimit_cfg_W132_TW.conf \
"
SRC_URI[bundle.md5sum] = "2cb8eca93c2e9b82ca3553dec4d4e491"
SRC_URI[bundle.sha256sum] = "6add26a274e31843db63e2f5c238a263accf77ff94a9f0aef29e6c129fb8b3d0"

S = "${WORKDIR}/USB-8801-U16-X86-W${PV}_B0-MGPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

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
    tar -xf ${MRVL_PREAMBLE}-W${PV}_B0-MGPL.tar
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
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    oe_runmake \
        KERNELDIR="${STAGING_KERNEL_BUILDDIR}" \
        CC="${KERNEL_CC} -fgnu89-inline" \
        LD="${KERNEL_LD}" \
        default

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    oe_runmake \
        KERNELDIR="${STAGING_KERNEL_BUILDDIR}" \
        CC="${CC} ${LDFLAGS} -fgnu89-inline" \
        LD="${LD}" \
        AS="${AS}" \
        build -o default
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

    # Copy config files
    cp -r README* config ${D}${INSTALLDIR}/

    # copy modprobe config files for prevent loading of kernel builtin
    # mwifiex modules and concurrent modules
    install -d ${D}${sysconfdir}/modprobe.d
    cp -r ${WORKDIR}/mrvl-usb8801.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # Install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${INSTALLDIR}/config/"
    done
}

# Install usbcore wakeup workaround for TK1 as udev rule
do_install_append_apalis-tk1() {
    install -d ${D}${sysconfdir}/udev/rules.d/
    install -m 0644 ${WORKDIR}/lily-w1.rules ${D}${sysconfdir}/udev/rules.d/
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/mrvl/*"

FILES_${PN}-tools = " \
    ${INSTALLDIR}/* \
    ${INSTALLDIR}/config/* \
    ${INSTALLDIR}/wifidirect/* \
    ${INSTALLDIR}/wifidisplay/* \
    ${sysconfdir}/udev/rules.d/* \
"

FILES_${PN}-dbg = "${INSTALLDIR}/.debug/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "lily-w1-driver"
