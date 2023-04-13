#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Open source driver and matching firmware for JODY-W1 (BCM89359)"

LICENSE = "GPLv2 & cypress-proprietary"
LIC_FILES_CHKSUM = " \
    file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7 \
    file://${WORKDIR}/cypress-hostap_${PV_HOSTAP}/COPYING;md5=292eece3f2ebbaa25608eed8464018a3 \
    file://${WORKDIR}/hostap_${PV_HOSTAP}/COPYING;md5=292eece3f2ebbaa25608eed8464018a3 \
"

PR = "r0"
PV = "4.14.77-2019_0503"
PV_HOSTAP = "2_6"
BUNDLE = "cypress-fmac"

MODULE_NAME = "jody-w1"
POSTFIX = "fmac"
FW_VER = "9.40.115"
DRIVER_VER = "4.14.77"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/${BUNDLE}-v${PV}.zip${LULA_DL_PARAMS};name=bundle \
    https://w1.fi/cgit/hostap/snapshot/hostap_${PV_HOSTAP}.tar.gz;name=hostap \
    file://${PV}/0001-cfg80211-sysfs-use-dev_attrs-before-linux-3.13.patch \
    file://${PV}/0002-defconfigs-brcmfmac-enable-debug.patch \
    file://jody-w1-driver-fmac.conf \
    file://defconfig.hostapd \
    file://defconfig.wpa_supplicant \
    file://wl \
"
SRC_URI[bundle.md5sum] = "c094b10e47b70b901ecf83f5480aee58"
SRC_URI[bundle.sha256sum] = "75767acfe8865e4e211097a8cd96a78e4ca170cde51e9cd806778cabc0bb2b9f"
SRC_URI[hostap.md5sum] = "f160437e301b4397922af2bad595c9af"
SRC_URI[hostap.sha256sum] = "7f4a7b93949eb93c93109c4cd02723eccd4d2f78fe17ebfa00564f168753f8bb"

S = "${WORKDIR}/v4.14.77-backports"

EXTRA_OEMAKE = "KLIB_BUILD=${STAGING_KERNEL_DIR} KLIB=${D} DESTDIR=${D}"

DEPENDS = "virtual/kernel jody-w1-nvrams libnl openssl"

inherit module-base ubx-getversion

addtask do_patch_hostap before do_configure after do_patch

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local file

    cd ${WORKDIR}
    for file in *.tar.gz; do
        tar -xzf $file
    done
}

do_patch_hostap() {
    local patch

    cd ${WORKDIR}/hostap_${PV_HOSTAP}
    for patch in ${WORKDIR}/cypress-hostap_2_6/*.patch; do
        patch -p1 < ${patch}
    done
}

do_configure[depends] += "virtual/kernel:do_deploy"

do_configure_prepend() {
    cp ${STAGING_KERNEL_BUILDDIR}/.config ${STAGING_KERNEL_DIR}
    CC=${BUILD_CC} oe_runmake -C kconf conf

    # Configure hostap
    cp -f ${WORKDIR}/defconfig.hostapd \
          ${WORKDIR}/hostap_${PV_HOSTAP}/hostapd/.config
    cp -f ${WORKDIR}/defconfig.wpa_supplicant \
          ${WORKDIR}/hostap_${PV_HOSTAP}/wpa_supplicant/.config
}

do_configure_append() {
    oe_runmake defconfig-brcmfmac
    # Get rid of some unnecessary errors at the install stage
    sed -i "s#@./scripts/update-initramfs## " Makefile
    sed -i "s#@./scripts/update-initramfs $(KLIB)## " Makefile.real
    sed -i "s#@./scripts/check_depmod.sh## " Makefile.real
    sed -i "s#@/sbin/depmod -a## " Makefile.real
}

do_compile() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    oe_runmake \
        KERNEL_PATH=${STAGING_KERNEL_DIR} \
        KERNEL_SRC=${STAGING_KERNEL_DIR} \
        KERNEL_VERSION=${KERNEL_VERSION} \
        CC="${KERNEL_CC}" \
        LD="${KERNEL_LD}" \
        AR="${KERNEL_AR}" \
        ${MAKE_TARGETS}

    # Compile hostap
    cd ${WORKDIR}/hostap_${PV_HOSTAP}/hostapd
    oe_runmake \
        LDFLAGS="${LDFLAGS}" \
        LIBNL_INC="${STAGING_INCDIR}/libnl3"
    cd ${WORKDIR}/hostap_${PV_HOSTAP}/wpa_supplicant
    oe_runmake \
        LDFLAGS="${LDFLAGS}" \
        LIBNL_INC="${STAGING_INCDIR}/libnl3"
}

do_install() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    oe_runmake \
        DEPMOD=echo \
        INSTALL_MOD_PATH="${D}" \
        KERNEL_SRC=${STAGING_KERNEL_DIR} \
        CC="${KERNEL_CC}" \
        LD="${KERNEL_LD}" \
        modules_install

    install -d ${D}/lib/firmware/brcm
    install -m 0644 ${WORKDIR}/firmware/brcmfmac4359* ${D}/lib/firmware/brcm/

    install -d ${D}${sysconfdir}/modprobe.d
    install -m 0644 ${WORKDIR}/jody-w1-driver-fmac.conf ${D}${sysconfdir}/modprobe.d/

    # Use JODY-W164-03A for PCIe
    ln -rs ${D}/lib/firmware/brcm/jody-w1-pcie/jody-w164-03a.nvram \
           ${D}/lib/firmware/brcm/brcmfmac4359-pcie.txt
    # Use JODY-W164-07A for SDIO
    ln -rs ${D}/lib/firmware/brcm/jody-w1-sdio/jody-w164-07a.nvram \
           ${D}/lib/firmware/brcm/brcmfmac4359-sdio.txt

    install -d ${D}/opt/jody-w1/fmac
    install -m 0755 ${WORKDIR}/hostap_${PV_HOSTAP}/hostapd/hostapd ${D}/opt/jody-w1/fmac/
    install -m 0755 ${WORKDIR}/hostap_${PV_HOSTAP}/hostapd/hostapd_cli ${D}/opt/jody-w1/fmac/
    install -m 0755 ${WORKDIR}/hostap_${PV_HOSTAP}/wpa_supplicant/wpa_supplicant ${D}/opt/jody-w1/fmac/
    install -m 0755 ${WORKDIR}/hostap_${PV_HOSTAP}/wpa_supplicant/wpa_cli ${D}/opt/jody-w1/fmac/
    install -m 0755 ${WORKDIR}/wl ${D}/opt/jody-w1/fmac/
}

PACKAGES += "${PN}-wl"
COMPATIBLE_HOST_${PN}-wl = "arm.*-linux"

FILES_${PN}-wl += "/opt/jody-w1/fmac/wl"

INSANE_SKIP_${PN} += "already-stripped"
FILES_${PN} += "\
    ${nonarch_base_libdir}/udev \
    ${sysconfdir}/udev \
    ${nonarch_base_libdir} \
    /opt/jody-w1/fmac/hostapd \
    /opt/jody-w1/fmac/hostapd_cli \
    /opt/jody-w1/fmac/wpa_supplicant \
    /opt/jody-w1/fmac/wpa_cli \
"

RDEPENDS_${PN} += "${PN}-wl"
RDEPENDS_${PN}-wl += "libnl-genl"
RDEPENDS_${PN}-wl += "libnl-nf"
RDEPENDS_${PN}-wl += "libnl-route"

RPROVIDES_${PN} += "jody-w1-driver"
