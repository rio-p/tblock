#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8987 Driver/Firmware/Tools for JODY-W2"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://mbt_src/README;beginline=4;endline=17;md5=cf9962a54f3749e5f4face3208a92369 \
    file://mbtc_src/README;beginline=4;endline=17;md5=cf9962a54f3749e5f4face3208a92369 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=ed3d33b970b2b6d7df82243b44c0adb2 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=19f974911e4edd25d773e0ccd426962b \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=d680cc7e22c1215bf4735eab9f70a7bc \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=5c5b58d643da4360ac6b77676c302b99 \
"

PR = "r0"
PV = "W16.68.10.p33-C4X16651"
MRVL_PV = "W16.68.10.p33-16.26.10.p33-C4X16651"

MODULE_NAME = "jody-w2"
POSTFIX = "sdio"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"
FW_VER = "W16.68.10.p33"
BT_FW_VER = "16.26.10.p33"
DRIVER_VER = "C4X16651"
INSTALLDIR = "/opt/${MODULE_NAME}/${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-SD-BT-8987-U16-MMC-${MRVL_PV}-MGPL.zip${LULA_DL_PARAMS};name=bundle \
    file://jody-w2-driver-sdio.conf \
    file://ed_mac_ctrl.conf \
    file://robust_btc.conf \
    file://txpwrlimits/txpower_default.bin \
    file://txpwrlimits/txpwrlimit_cfg_161-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_165-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_KCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://WlanCalData_ext.conf \
    file://WlanCalData_ext_5pF.conf \
"
SRC_URI[bundle.md5sum] = "8095adf5ebf082bc6752eb01f7743bef"
SRC_URI[bundle.sha256sum] = "62662ccd64f7ef0d95a080e97b6d14f661b416003fc4ea3d2f668c383d0cb456"

S = "${WORKDIR}/SD-UAPSTA-BT-8987-U16-MMC-${MRVL_PV}-MGPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

do_unpack[depends] = "unzip-native:do_populate_sysroot"

do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
}

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local tarball

    mv ${WORKDIR}/SD-WLAN-SD-BT-8987-U16-MMC-${MRVL_PV}-MGPL/*.tgz ${WORKDIR}/
    cp -r ${WORKDIR}/SD-WLAN-SD-BT-8987-U16-MMC-${MRVL_PV}-MGPL/FwImage ${WORKDIR}/
    rm -rf ${WORKDIR}/SD-WLAN-SD-BT-8987-U16-MMC-${MRVL_PV}-MGPL
    for tarball in ${WORKDIR}/*.tgz; do
        tar -xzf ${tarball} -C ${WORKDIR}
    done
}

EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="mbt_src mbtc_src wlan_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        module_do_compile default
    done

    for i in ${dirs}; do
        cd ${S}/${i}
        # Only build apps
        oe_runmake \
            CC="${CC} ${LDFLAGS} -fgnu89-inline" \
            LD="${LD}" \
            AS="${AS}" \
            build -o default
    done
}

do_install() {
    # Install firmware images
    install -d ${D}/lib/firmware/mrvl
    local fw
    for fw in ${WORKDIR}/FwImage/*.bin; do
        install -m 0644 \
            ${fw} \
            ${D}/lib/firmware/mrvl/$(basename $(printf '%s' "${fw%.bin}_${MODULE_IDENTITY}.bin"))
    done
    # Install calibration data for early samples
    install -d ${D}/lib/firmware/mrvl/${MODULE_IDENTITY}
    install -m 0644 \
        ${WORKDIR}/WlanCalData_ext.conf \
        ${D}/lib/firmware/mrvl/${MODULE_IDENTITY}/WlanCalData_ext.conf
    install -m 0644 \
        ${WORKDIR}/WlanCalData_ext_5pF.conf \
        ${D}/lib/firmware/mrvl/${MODULE_IDENTITY}/WlanCalData_ext_5pF.conf

    # Install Wi-Fi driver
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987
    install -m 0644 \
        ${S}/bin_sd8987/mlan.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/mlan_${MODULE_IDENTITY}.ko
    install -m 0644 \
        ${S}/bin_sd8987/sd8987.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/sd8987_${MODULE_IDENTITY}.ko

    # Install Bluetooth driver
    install -d ${D}/${INSTALLDIR}/
    install -m 0644 \
        ${S}/bin_sd8987_bt/bt8987.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/bt8987_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_sd8987_bt/fmapp ${D}/${INSTALLDIR}/
    install -m 0644 ${S}/bin_sd8987_bt/README ${D}${INSTALLDIR}/README_BT

    # Install Bluetooth char driver
    install -m 0644 \
        ${S}/bin_sd8987_btchar/mbt8987.ko \
        ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8987/mbt8987_${MODULE_IDENTITY}.ko
    install -m 0644 ${S}/bin_sd8987_btchar/README ${D}${INSTALLDIR}/README_MBT

    # Install modprobe config
    install -d ${D}/${sysconfdir}/modprobe.d
    cp ${WORKDIR}/jody-w2-driver-sdio.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # Install tools and utilities
    install -m 0755 ${S}/bin_sd8987/mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_sd8987/mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 ${S}/bin_sd8987/mlanutl ${D}${INSTALLDIR}/
    install -m 0755 ${S}/bin_sd8987/uaputl.exe ${D}${INSTALLDIR}/uaputl
    cd ${D}/${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 ${S}/bin_sd8987/wifidirectutl ${D}${INSTALLDIR}/

    # Install config files, scripts, helpers and docs
    cp -r ${S}/bin_sd8987/config ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_sd8987/wifidirect ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_sd8987/wifidisplay ${D}/${INSTALLDIR}/
    cp -r ${S}/bin_sd8987/README* ${D}/${INSTALLDIR}/

    # Install energy detection configuration for EVK-JODY-W2
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}/${INSTALLDIR}/config/
    # Install modified rbc hostcmd config file for EVK-JODY-W2
    install -m 0644 ${WORKDIR}/robust_btc.conf ${D}/${INSTALLDIR}/config/
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/*"

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
RPROVIDES_${PN} += "jody-w2-driver"
