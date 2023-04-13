#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "BCM DHD driver and firmware for JODY-W1 (BCM89359)"

DEPENDS = "libnl openssl"

LICENSE = "GPLv2 & Apache-2.0 & BSD-3-Clause & cypress-proprietary"
LIC_FILES_CHKSUM = " \
    file://${S}/dhd.h;beginline=7;endline=27;md5=55e36d4cdca5fc6d05f00dce7174f487 \
    file://${WORKDIR}/Android_O/bcmdhd-hal/wifi_hal/common.h;beginline=2;endline=16;md5=e5867f498d77ded7d9c5b7d8d836922e \
    file://${WORKDIR}/hostapd_supplicant_src/linux/hostap/hostapd/README;beginline=5;endline=47;md5=5aa8a39a75b7f47f79d373a35a255dfc \
"

PR = "r2"
BUNDLE = "BRAKEPAD"
S = "${WORKDIR}/89359_SDIO/host_bcmdhd_src/bcmdhd"
MODULE_NAME = "jody-w1"
KO_NAME = "jody_w1_sdio.ko"

POSTFIX = "sdio"
FW_VER = "9.40.112.1"
DRIVER_VER = "1.363.68"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

# avoid backwards incompatibility.
PE = "1"

SRC_URI = " \
    ${LULA_DL_URL}/${BUNDLE}-89359-SDIO.zip${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-adjust-makefile.patch \
    file://${PV}/0002-switch-from-vfs_write-to-kernel_write.patch \
    file://jody-w1-driver-sdio.conf \
"
SRC_URI[bundle.md5sum] = "54ad97212c9a53a301a1cc54da78672e"
SRC_URI[bundle.sha256sum] = "9b1cbf70672d7002b32ee6ff4fabc9d3a37d90e4e2251ed316504a0cbb3bfc25"

inherit module ubx-sanitize ubx-compile-qa ubx-getversion

do_unpack[depends] += "p7zip-native:do_populate_sysroot"
do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd ${WORKDIR}

    for file in ${BUNDLE}*.7z; do
        if test -f $file; then
            7z x -y $file
        fi
    done

    # Building hostapd and wpa_supplicant from same source tree doesn't
    # work because object files need to be build with different
    # compilation options. We just duplicate the sources here.
    cp -rp hostapd_supplicant_src/linux/hostap \
           hostapd_supplicant_src/linux/wpa_supplicant
}

do_configure() {
    cd ${WORKDIR}/hostapd_supplicant_src/linux/wpa_supplicant/wpa_supplicant
    [ ! -e .config.bak ] && cp .config .config.bak
    cp .config.bak .config
    echo "CFLAGS +=\"-I${STAGING_INCDIR}/libnl3\"" >> .config
    echo "DRV_CFLAGS +=\"-I${STAGING_INCDIR}/libnl3\"" >> .config

    cd ${WORKDIR}/hostapd_supplicant_src/linux/hostap/hostapd
    [ ! -e .config.bak ] && cp .config .config.bak
    cp .config.bak .config
    echo "undefine CONFIG_LIBNL20" >> .config
    echo "CONFIG_LIBNL32=y" >> .config
    echo "CONFIG_NO_RADIUS=y" >> .config
    echo "CONFIG_IEEE80211AC=y" >> .config
    echo "CONFIG_ACS=y" >> .config

    ubx_sanitize_kernel_config_is_set "CONFIG_MMC"
    return $?
}

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    DHDCFLAGS=-DNO_SDIO_RESET \
        oe_runmake \
                KDIR="${STAGING_KERNEL_BUILDDIR}" \
                CONFIG_BCMDHD_SDIO=y \
                CONFIG_BCMDHD=m \
                CONFIG_BCM4359=y \
                CONFIG_BCMDHD_SDMMC=y \
                CC="${KERNEL_CC}" \
                LD="${KERNEL_LD}"

    cd ${WORKDIR}/hostapd_supplicant_src/linux/wpa_supplicant/libbcmdhd
    oe_runmake CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}"
    cd ${WORKDIR}/hostapd_supplicant_src/linux/wpa_supplicant/wpa_supplicant
    oe_runmake CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}"

    export CFLAGS="-MMD -O2 -Wall -g -I${STAGING_INCDIR}/libnl3"
    cd ${WORKDIR}/hostapd_supplicant_src/linux/hostap/libbcmdhd
    oe_runmake CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}"
    cd ${WORKDIR}/hostapd_supplicant_src/linux/hostap/hostapd
    oe_runmake CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}"
}

do_install() {
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/brcm
    install -m 0644 ${S}/bcmdhd.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/brcm/${KO_NAME}

    install -d ${D}/lib/firmware/brcm/jody-w1-sdio
    install -m 0644 ${WORKDIR}/89359_SDIO/wlan_firmwares/fw_*.bin ${D}/lib/firmware/brcm/jody-w1-sdio/
    install -m 0644 ${WORKDIR}/89359_SDIO/clm_blob/*.clm_blob ${D}/lib/firmware/brcm/jody-w1-sdio/

    install -d ${D}${sysconfdir}/modprobe.d
    install -m 0644 ${WORKDIR}/jody-w1-driver-sdio.conf ${D}${sysconfdir}/modprobe.d/

    install -d ${D}/opt/${MODULE_NAME}/sdio
    cd ${WORKDIR}/hostapd_supplicant_src/linux/wpa_supplicant/wpa_supplicant
    install -m 755 wpa_supplicant	${D}/opt/${MODULE_NAME}/sdio/
    install -m 755 wpa_cli		${D}/opt/${MODULE_NAME}/sdio/
    cd ${WORKDIR}/hostapd_supplicant_src/linux/hostap/hostapd
    install -m 755 hostapd		${D}/opt/${MODULE_NAME}/sdio/
    install -m 755 hostapd_cli      ${D}/opt/${MODULE_NAME}/sdio/
}

PACKAGES += "${PN}-firmware ${PN}-wpa-supplicant ${PN}-hostapd"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

FILES_${PN}-firmware = "/lib/firmware/brcm/jody-w1-sdio/*"

FILES_${PN}-wpa-supplicant = " \
    /opt/${MODULE_NAME}/sdio/wpa_* \
"

FILES_${PN}-hostapd = " \
    /opt/${MODULE_NAME}/sdio/hostapd* \
"

FILES_${PN}-dbg += " \
    /opt/${MODULE_NAME}/sdio/.debug/* \
    /usr/src/debug/* \
"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-wpa-supplicant ${PN}-hostapd"
RDEPENDS_${PN} += "kernel-module-jody-w1-sdio"
RDEPENDS_${PN} += "jody-w1-nvrams"

RPROVIDES_${PN} += "jody-w1-driver"
