#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "VERA-P1 LLC Remote Driver/Firmware/Tools"

LICENSE = "BSD-3-Clause & cohda-firmware"
LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/LICENCE.Cohda;md5=deb66e3962995fa633dd49f59302a574 \
    file://${WORKDIR}/llc-remote/bsp/app/gpsd/COPYRIGHT.Note;md5=fb43c2ce0376797f4d634a3d8a207436 \
    file://${WORKDIR}/llc-remote/bsp/app/libpcap/LICENSE;md5=5eb289217c160e2920d2e35bddc36453 \
"

DEPENDS = "libusb libpcap udev vim-native"

#COMPATIBLE_HOST = "arm.*-linux"

PR = "r9"
PV = "15.0.0"

MODULE_NAME = "vera-p1"
POSTFIX = "llc"
FW_VER = "69189"
DRIVER_VER = "15.0.0"
INSTALLDIR = "/opt/${MODULE_NAME}"

SRC_URI = " \
    ${LULA_DL_URL}/V2X_LLC_Remote_V${PV}.tar.gz${LULA_DL_PARAMS};name=bundle \
    ${LULA_DL_URL}/vera-p1-driver-llc-remote-plugins-${PV}-aarch64.tgz;name=plugins-bin \
    file://${PV}/0001-fix-compilation-issues.patch \
    file://${PV}/0002-fix-udev-libusb-issues.patch \
    file://${PV}/0003-add-compile-time-assertions-for-LLCListItem.patch \
    file://${PV}/0004-add-automatic-calculation-for-user-data-size.patch \
    file://${PV}/0005-enable-plugin-binaries-for-aarch64.patch \
    file://LICENCE.Cohda \
    file://vera-p1.rules \
    file://load.sh \
    file://cal.sh \
"
SRC_URI[bundle.md5sum] = "d78f88c33a98abec192d42fea76b8654"
SRC_URI[bundle.sha256sum] = "fb53d403202ab37b9c15fa55edd1a81c49ba57a0732951e1d9e4b5c9bc03a05a"
SRC_URI[plugins-bin.md5sum] = "b0becdc14c86f385d2d4d74a22daa66b"
SRC_URI[plugins-bin.sha256sum] = "42b79966cea241a0545e3015101d91e9daf90dbd1e09d136228ee122da9ed721"

S = "${WORKDIR}/llc-remote/"

do_unpack[cleandirs] += "${S}"

CLEANBROKEN = "1"

inherit module ubx-compile-qa ubx-getversion

# Keep workarounds separated in this step
do_configure() {
    # Place missing FW files where cw-llc Makefile expects them to be
    cp ${S}/images/SDRMK5DualSPI.bin ${S}/cohda/kernel/drivers/cohda/llc
    cp ${S}/images/DFUBootLoader.bin ${S}/cohda/kernel/drivers/cohda/llc
    cp ${S}/images/FastBootSpi.usb ${S}/cohda/kernel/drivers/cohda/llc/FastBootSpi.bin
}

do_compile_prepend() {
    cp -f ${WORKDIR}/plugin/* ${S}/cohda/app/llc/plugin
}

# FIXME: this recipe has a known issue when compiled against a libusb < 1.0.21
# (observed with 1.0.9), since the udsloader binary (new deliverable since PV=15.0.0)
# has a dependency to the symbol libusb_strerror a cross develop environment needs
# to provide the package libusb in the version higher than 1.0.21.
# ubx-ref (TE_TP1-146)
#

EXTRA_OEMAKE += "BOARD=mk5 KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    cd ${S}/cohda/kernel/drivers/cohda/llc
    module_do_compile

    cd ${S}/cohda/app/llc
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    make \
        BOARD=mk5 \
        CC="${CC} ${LDFLAGS} -fgnu89-inline" \
        LD="${LD}" \
        AS="${AS}"

    cd ${S}/cohda/app/uds-loader
    oe_runmake \
        BOARD=mk5 \
        ROOTFS="${STAGING_DIR_TARGET}" \
        CC="${CC}" \
        CFLAGS="${CFLAGS}" \
        LDFLAGS="${LDFLAGS}"
}

do_install() {
    install -d ${D}/lib/firmware/cohda
    cd ${S}/cohda/kernel/drivers/cohda/llc
    install -m 0644 ${S}/images/SDRMK5*.bin ${D}/lib/firmware/cohda/
    ln -sf SDRMK5Dual.bin ${D}/lib/firmware/cohda/SDRMK5Firmware.bin
    install -m 0644 ${WORKDIR}/LICENCE.Cohda ${D}/lib/firmware/cohda/
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/cohda
    install -m 0644 cw-llc.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/cohda/

    install -d ${D}${libdir}
    install -d ${D}/opt/${MODULE_NAME}/llc
    cd ${S}/cohda/app/llc
    install -m 0755 lib/libLLC.so ${D}${libdir}/libLLC.so
    install -m 0755 llc ${D}/opt/${MODULE_NAME}/llc/
    install -d ${D}/opt/${MODULE_NAME}/llc/plugin
    install -m 0755 plugin/*.so ${D}/opt/${MODULE_NAME}/llc/plugin/
    # remove plugins depending on libgps and example
    rm -f ${D}/opt/${MODULE_NAME}/llc/plugin/example.so
    rm -f ${D}/opt/${MODULE_NAME}/llc/plugin/llc-utc.so
    rm -f ${D}/opt/${MODULE_NAME}/llc/plugin/llc-echo.so

    install -m 0755 ${S}/cohda/app/uds-loader/uds-loader ${D}/opt/${MODULE_NAME}/
    install -m 0644 ${S}/cohda/app/uds-loader/FastBootSpiUds.usb ${D}/lib/firmware/cohda/

    install -d ${D}${includedir}/cohda/pktbuf
    cp -r ${S}/cohda/kernel/include/* ${D}${includedir}
    cp ${S}/cohda/app/pktbuf/*.h ${D}${includedir}/cohda/pktbuf/

    install -d ${D}${sysconfdir}/udev/rules.d/
    install -m 0644 ${WORKDIR}/vera-p1.rules ${D}${sysconfdir}/udev/rules.d/
    install -m 0755 ${WORKDIR}/load.sh ${D}/opt/${MODULE_NAME}/
    install -m 0755 ${WORKDIR}/cal.sh ${D}/opt/${MODULE_NAME}/
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/cohda/*"

FILES_${PN} += " \
    ${libdir}/*.so* \
    ${includedir}/*.h \
    ${sysconfdir}/udev/rules.d/* \
    /opt/${MODULE_NAME}/*.sh \
"

# prevent putting the .so to the dev package
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES_${PN}-tools = " \
    /opt/${MODULE_NAME}/* \
    /opt/${MODULE_NAME}/llc/* \
    /opt/${MODULE_NAME}/llc/plugin/* \
"

FILES_${PN}-dbg = " \
    /opt/${MODULE_NAME}/llc/.debug/* \
    /opt/${MODULE_NAME}/llc/plugin/.debug/* \
    /opt/${MODULE_NAME}/.debug/* \
    /usr/lib/.debug/* \
    /usr/src/debug* \
"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-tools dfu-util (>= 0.5)"
RDEPENDS_${PN} += "kernel-module-cw-llc"

RPROVIDES_${PN} += "vera-p1-driver"

RCONFLICTS_${PN} += "theo-p1-driver"
RREPLACES_${PN} += "theo-p1-driver"
