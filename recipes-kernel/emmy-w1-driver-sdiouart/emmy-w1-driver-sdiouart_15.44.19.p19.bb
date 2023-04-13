#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8887 Driver/Firmware/Tools for EMMY-W1"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=8f0e13ce436472d732143494812a38df \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=9a3e80eb5ab3077324e79060c898bde8 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=19f974911e4edd25d773e0ccd426962b \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=3cf02fa4277efdc47097168ee8c3c34c \
    file://muart_src/include/bluetooth.h;beginline=2;endline=22;md5=a9368317a4d8c4a203eaba91f019469f \
    file://muart_src/bt_drv.h;beginline=6;endline=19;md5=820cde90685c7fc9b46427e8de72c804 \
    file://muart_src/bt_drv.h;beginline=27;endline=44;md5=0ef5e9546509f8f0e951f5e41b142cf7 \
    file://uartfwloader_src/src/fw_loader_uart.h;beginline=5;endline=23;md5=ba4da79ccbbd4f6ec6e118fdc6141729 \
"

PR = "r1"
PV = "15.44.19.p19-C4X15632"
MRVL_PV = "15.44.19.p19-15.100.19.p19-C4X15632"

MODULE_NAME = "emmy-w1"
POSTFIX = "sdiouart"
FW_VER = "15.44.19.p19"
BT_FW_VER = "15.100.19.p19"
DRIVER_VER = "C4X15632"
INSTALLDIR = "/opt/${MODULE_NAME}/sd8887-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-WLAN-UART-BT-8887-U16-MMC-W${MRVL_PV}_A2-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${MRVL_PV}/0001-uartfwloader-make-default-CC-optional.patch \
    file://mrvl-sd8887.conf \
    file://ed_mac_ctrl.conf \
    file://robust_btc.conf \
    file://txpwrlimits/txpower_default.bin \
    file://txpwrlimits/txpwrlimit_cfg_161-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_165-FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_KCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_GITEKI.conf \
    file://txpwrlimits/txpwrlimit_cfg_SRRC.conf \
"
SRC_URI[bundle.md5sum] = "61edd0c8f5e09cd20eb94d66fc2d9a34"
SRC_URI[bundle.sha256sum] = "7d93b3d3077ffbcec5cfde4fd93fb13456f84c6980b6099d4c94a6d643fb2755"

S = "${WORKDIR}/SD-UAPSTA-UART-BT-8887-U16-MMC-W${MRVL_PV}_A2-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

# since txpowerlimits class can only resolve FCC ETSI and JP
do_generate_txpower_bins_append() {
    ln -f -s txpower_KCC.bin txpower_KR.bin;
    ln -f -s txpower_SRRC.bin txpower_CN.bin;
}

# Make sure unrar is available before unpack is starting.
do_unpack[depends] = "unzip-native:do_populate_sysroot"
do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i
    local inter_bundle="SD-WLAN*-GPL"

    cd "${WORKDIR}"
    rm -rf FwImage app_uart8887d
    mv ${inter_bundle}/* .
    rmdir ${inter_bundle}

    for i in *.tgz; do
        tar -xzf ${i}
    done
}

EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        ## in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="wlan_src muart_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        #only build kernel modules not the apps
        module_do_compile default
    done

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags cant be added to the CFLAGS since
    # defining those would break the Makefile. flags needs to be added as work
    # around new inline bahaviour in gcc-5
    # be advised, oe_runmake has been poluted from module bbclass and should not be used for building apps.
    for i in ${dirs}; do
        cd ${S}/${i}
        #only build apps
        oe_runmake \
            CC="${CC} ${LDFLAGS} -fgnu89-inline" \
            LD="${LD}" \
            AS="${AS}" \
            build -o default
    done

    cd ${S}/uartfwloader_src/linux
    oe_runmake makelinks
    oe_runmake make CC="${CC} ${LDFLAGS}" TARGET=W8887
}

do_install() {
    # Install firmware images
    install -d "${D}"/lib/firmware/mrvl/
    cd "${D}"/lib/firmware/mrvl
    install -m 0644 "${WORKDIR}"/FwImage/sd8887_wlan_a2.bin ./sd8887_wlan_a2_${POSTFIX}.bin
    install -m 0644 "${WORKDIR}"/FwImage/sduart8887_combo_a2.bin ./sd8887_uapsta_a2_${POSTFIX}.bin
    install -m 0644 "${WORKDIR}"/FwImage/uart8887_bt_a2.bin ./uart8887_bt_a2_${POSTFIX}.bin

    # Install Wi-Fi driver
    cd ${S}/bin_sd8887
    install -d "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887
    install -m 0644 mlan.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/mlan_sd8887_${POSTFIX}.ko
    install -m 0644 sd8887.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/sd8887_${POSTFIX}.ko

    # Install config files, scripts, helpers and docs
    install -d "${D}"${INSTALLDIR}/
    cp -r config "${D}"${INSTALLDIR}/
    cp -r wifidirect wifidisplay "${D}"${INSTALLDIR}/
    cp -r README* "${D}"${INSTALLDIR}/

    # Install tools and utilities
    install -m 0755 mlan2040coex "${D}"${INSTALLDIR}/
    install -m 0755 mlanevent.exe "${D}"${INSTALLDIR}/mlanevent
    install -m 0755 mlanutl "${D}"${INSTALLDIR}/
    install -m 0755 uaputl.exe "${D}"${INSTALLDIR}/uaputl
    cd "${D}"${INSTALLDIR}
    ln -s uaputl uaputl.exe
    cd -
    install -m 0755 wifidirectutl "${D}"${INSTALLDIR}/
    install -m 0755 ${S}/uartfwloader_src/linux/fw_loader_W8887 ${D}/${INSTALLDIR}/fw_loader
    find ${S}/uartfwloader_src/linux -type l -exec rm -rf {} \;

    # Install Bluetooth driver
    cd ${S}/bin_muart
    install -m 0644 hci_uart.ko "${D}"/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8887/hci_uart_${POSTFIX}.ko
    install -m 0644 README "${D}"${INSTALLDIR}/README_HCI_UART

    # Install modprobe config
    install -d "${D}"${sysconfdir}/modprobe.d
    cp -r "${WORKDIR}"/mrvl-sd8887.conf "${D}"${sysconfdir}/modprobe.d/${PN}.conf

    # install energy detection configuration for EVK-EMMY-W1
    install -m 0644 "${WORKDIR}"/ed_mac_ctrl.conf "${D}"${INSTALLDIR}/config/

    # install modified rbc hostcmd config file for EVK-EMMY-W1
    install -m 0644 "${WORKDIR}"/robust_btc.conf "${D}"${INSTALLDIR}/config/

    # install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${INSTALLDIR}/config/"
    done
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
RPROVIDES_${PN} += "emmy-w1-driver"

RPROVIDES_kernel-module-mlan-sd8887-${POSTFIX} += "kernel-module-mlan"
