#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "SD8787 Driver/Firmware/Tools"

LICENSE = "GPL-2 & marvell-firmware & marvell-confidential"
LIC_FILES_CHKSUM = " \
    file://wlan_src/gpl-2.0.txt;md5=ab04ac0f249af12befccb94447c08b77 \
    file://mbt_src/README;beginline=4;endline=17;md5=0b3c87eeba2fa13d89d7795d88d36899 \
    file://mbtc_src/README;beginline=4;endline=17;md5=0b3c87eeba2fa13d89d7795d88d36899 \
    file://wlan_src/mapp/mlanutl/mlanutl.h;beginline=5;endline=22;md5=617b6a5d0cf1f8c94c7d49e52db6ebad \
    file://wlan_src/mlinux/mlan.h;beginline=6;endline=24;md5=aebe1e8ec8e9459aeedaef9116105e64 \
    file://wlan_src/mlan/mlan.h;beginline=6;endline=24;md5=aebe1e8ec8e9459aeedaef9116105e64 \
"

PR = "r1"
PV = "W14.66.35.p65-M3X14539"
MODULE_NAME = "ella-w1"
POSTFIX = "industrial"
FW_VER = "W14.66.35.p65"
DRIVER_VER = "M3X14539"
INSTALLDIR = "/opt/${MODULE_NAME}/sd8787-${POSTFIX}"

SRC_URI = " \
    ${LULA_DL_URL}/SD-UAPSTA-BT-8787-FC18-MMC-${PV}_AX-GPL.zip${LULA_DL_PARAMS};name=bundle \
    file://ed_mac_ctrl.conf \
    file://mrvl-sd8787.conf \
    file://txpwrlimits/txpwrlimit_cfg_ETSI.conf \
    file://txpwrlimits/txpwrlimit_cfg_FCC.conf \
    file://txpwrlimits/txpwrlimit_cfg_JP.conf \
    file://txpwrlimits/txpwrlimit_cfg_TW.conf \
    file://txpwrlimits/txpwrlimit_cfg_W131_TW.conf \
"
SRC_URI[bundle.md5sum] = "70da9d9f17a3dca78781b9a72c6ee6a2"
SRC_URI[bundle.sha256sum] = "62513c0cb83d3b6e65f39e769c0ca38a75d6109a8c2145e1b3f564c973dbe2b9"

RPROVIDES_${PN} += "ella-w1-driver"

S = "${WORKDIR}/SD-UAPSTA-BT-8787-FC18-MMC-${PV}_AX-GPL"

inherit module ubx-mrvl-txpowerlimits ubx-compile-qa ubx-getversion

do_generate_txpower_bins_append() {
    ln -s txpower_JP.bin txpower_GITEKI.bin;
}

do_unpack[depends] = "unrar-native:do_populate_sysroot"

do_unpack[cleandirs] += "${S}"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    local i

    cd ${WORKDIR}
    cp -rf SD-UAPSTA-BT-8787-FC18-MMC-${PV}_AX-GPL/*.tar .

    for i in *.tar; do
        tar -xf ${i}
    done
    for i in *.tgz; do
        tar -xzf ${i}
    done
}

EXTRA_OEMAKE += "KERNELDIR=${STAGING_KERNEL_BUILDDIR}"

do_compile() {
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    local dirs="mbt_src mbtc_src wlan_src";
    for i in ${dirs}; do
        cd ${S}/${i}
        # only build kernel modules not the apps
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
}

do_install() {
    install -d ${D}${INSTALLDIR}
    install -d ${D}/lib/firmware/mrvl

    install -m 0644 ${WORKDIR}/FwImage/sd8787_uapsta.bin ${D}/lib/firmware/mrvl/sd8787_uapsta_${POSTFIX}.bin

    cd ${S}/bin_sd8787
    install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787
    install -m 0644 mlan.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/mlan_sd8787_${POSTFIX}.ko
    install -m 0644 sd8787.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/sd8787_${POSTFIX}.ko

    install -m 0755 mlan2040coex ${D}${INSTALLDIR}/
    install -m 0755 mlanevent.exe ${D}${INSTALLDIR}/mlanevent
    install -m 0755 mlanutl ${D}${INSTALLDIR}/
    install -m 0755 uaputl.exe ${D}${INSTALLDIR}/uaputl
    install -m 0755 wifidirectutl ${D}${INSTALLDIR}/

    # copy config files
    cp -r README* config wifidirect wifidisplay ${D}${INSTALLDIR}/

    cd ${S}/bin_sd8787_bt
    install -m 0644 bt8787.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/mrvl/sd8787/bt8787_${POSTFIX}.ko
    install -m 0755 fmapp ${D}${INSTALLDIR}/
    cp -r config ${D}${INSTALLDIR}/

    # copy modprobe config files for prevent loading of kernel builtin mwifiex modules and concurrent modules
    install -d ${D}${sysconfdir}/modprobe.d
    cp ${WORKDIR}/mrvl-sd8787.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf

    # install energy detection configuration for EVK-ELLA-W1
    install -m 0644 ${WORKDIR}/ed_mac_ctrl.conf ${D}${INSTALLDIR}/config/

    # install txpwrlimit_conf files
    for i in "${WORKDIR}"/txpwrlimits/txpwrlimit_cfg*; do
        install -m 0644 "${i}" "${D}${INSTALLDIR}/config/"
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
