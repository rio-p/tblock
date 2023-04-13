#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8787 Driver/Firmware/Tools"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = "\
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://mbtc_src/README;beginline=4;endline=17;md5=03e19937e4ef07ed76e3f88aecafb742 \
    file://mbt_src/README;beginline=4;endline=17;md5=03e19937e4ef07ed76e3f88aecafb742 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=9c641850857c6c78b1f3cfc3047d7c52 \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=19;md5=629bbf23e935d47bc20f55127b7b83ed \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=fb374294718d62d65d3d2a76470e2a47 \
    file://wlan_src/mapp/uaputl/uaputl.h;beginline=5;endline=22;md5=613079d4dd693766317866bacc43fd72 \
"

PR = "r10"
MRVL_PREAMBLE = "SD-UAPSTA-BT-FM-8787-FC13-MMC"
PV = "14.44.35.p238-M2614525"
PV_BUNDLE = "14.44.35.p238-M2614525"

#"SD-UAPSTA-BT-FM-8787-FC13-MMC-14.44.35.p238-M2614525_AX-GPL.zip"

MODULE_NAME = "ella-w1"
POSTFIX = "automotive"
MODULE_IDENTITY = "${MODULE_NAME}-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/${MRVL_PREAMBLE}-${PV_BUNDLE}_AX-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://${PV}/0001-set-iftype-ap-for-uap.patch \
    file://${PV}/0002-adapt-to-kernel-API-changes-4.4.patch \
    file://ed_mac_ctrl.conf \
    file://mrvl-sd8787.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_JP.conf \
    file://txpwrlimits/txpwrlimit_cfg_TW.conf \
    file://txpwrlimits/txpwrlimit_cfg_W131_TW.conf \
"

SRC_URI[bundle.md5sum] = "4814ee60b17d8db1bcb256c7510f1e20"
SRC_URI[bundle.sha256sum] = "def41e074307bd36b26e9c236ebec08e84abb8c6b5fed63b99dd71c1636e3f6e"

S = "${WORKDIR}/SD-UAPSTA-BT-FM-8787-FC13-MMC-${PV_BUNDLE}_AX-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa

do_generate_txpower_bins_append() {
    ln -s txpower_JP.bin txpower_GITEKI.bin;
}

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd ${WORKDIR}
#    echo "extracting ${MRVL_PREAMBLE}-${PV}_AX-GPL.zip ..."
#    unzip ${MRVL_PREAMBLE}-${PV}_AX-GPL.zip
    tar -xf ${MRVL_PREAMBLE}-${PV}_AX-GPL.tar
    for i in *.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        ## in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="mbt_src wlan_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        #only build kernel modules not the apps
        oe_runmake KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${KERNEL_CC}" LD="${KERNEL_LD}" AS="${KERNEL_AS}" default
    done

    # only building apps. The compiler might differ for apps and kernel.
    # be advised, those flags can't be added to the CFLAGS since
    # defining those would break the Makefile. Flags need to be added as a workaround
    # for the new inline bahaviour in gcc-5.
    # be advised, oe_runmake has been poluted from module bbclass and should not be used for building apps.
    for i in ${dirs}; do
        cd ${S}/${i}
        # only build apps
        make KERNELDIR="${STAGING_KERNEL_BUILDDIR}" CC="${CC} ${LDFLAGS} -fgnu89-inline" LD="${LD}" AS="${AS}" build -o default
    done
}

do_install() {
    installdir=/opt/${MODULE_NAME}/sd8787-automotive
    install -d ${D}${installdir}
    install -d ${D}/lib/firmware/mrvl

    install -m 0644 ${WORKDIR}/FwImage/sd8787_uapsta.bin ${D}/lib/firmware/mrvl/sd8787_uapsta_automotive.bin

    cd ${S}/bin_sd8787
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/mlan_sd8787_automotive.ko
    install -m 0644 sd8787.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/sd8787_automotive.ko

    install -m 0755 mlan2040coex ${D}${installdir}/
    install -m 0755 mlanevent.exe ${D}${installdir}/mlanevent
    install -m 0755 mlanutl ${D}${installdir}/
    install -m 0755 uaputl.exe ${D}${installdir}/uaputl
    install -m 0755 wifidirectutl ${D}${installdir}/

    # copy config files
    cp -r README* config wifidirect wifidisplay ${D}${installdir}/

    cd ${S}/bin_sd8787_bt
    install -m 0644 bt8787.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/bt8787_automotive.ko
    install -m 0755 fmapp ${D}${installdir}/
    cp -r config ${D}${installdir}/

    # copy modprobe config files for prevent loading of kernel builtin mwifiex modules and concurrent modules
    install -d ${D}${sysconfdir}/modprobe.d
    cp ${WORKDIR}/mrvl-sd8787.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # install energy detection configuration for EVK-ELLA-W1
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}${installdir}/config/
    # install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${installdir}/config/"
    done
}

PACKAGES += "${PN}-firmware ${PN}-tools"

FILES_${PN}-firmware = "/lib/firmware/mrvl/*"

FILES_${PN}-tools = " \
    /opt/${MODULE_NAME}/sd8787-${POSTFIX}/* \
    /opt/${MODULE_NAME}/sd8787-${POSTFIX}/config/* \
    /opt/${MODULE_NAME}/sd8787-${POSTFIX}/wifidirect/* \
    /opt/${MODULE_NAME}/sd8787-${POSTFIX}/wifidisplay/* \
"

FILES_${PN}-dbg = "/opt/${MODULE_NAME}/sd8787-${POSTFIX}/.debug/*"

FILES_${PN} += "${sysconfdir}/modprobe.d/*"

RDEPENDS_${PN} += "${PN}-firmware"
RDEPENDS_${PN} += "${PN}-tools"

RPROVIDES_${PN} += "kernel-module-mlan-${KERNEL_VERSION}"
RPROVIDES_${PN} += "kernel-module-mlan"
RPROVIDES_${PN} += "ella-w1-driver"
