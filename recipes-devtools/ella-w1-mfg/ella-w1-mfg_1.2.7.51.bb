#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

SUMMARY = "Manufacturing tools and firmware for the ELLA-W1 series"
DESCRIPTION = "Manufacturing tools and firmware for the ELLA-W1 series based on the Marvell 88W8787 chipset"

PR = "r6"
PV = "1.2.7.51-14.0.34.p4"
MODULE_NAME = "ella-w1"

LICENSE = "marvell-confidential & u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://bridge/mfgbridge.h;beginline=3;endline=4;md5=4279cf187b20e5381825657a4af65fbd \
    file://${WORKDIR}/load-modules.sh;beginline=1;endline=22;md5=f260a9c3da316d35f18a260b27de810c \
"

DEPENDS = "bluez5"

SRC_URI = "\
    ${LULA_DL_URL}/MFG-MF-8787-FC8-SYSKT-${PV}.zip${LULA_DL_PARAMS};name=mfgbundle \
    file://load-modules.sh \
    file://unload-modules.sh \
    file://ella-w1-mfgbridge \
    file://ella-w1-mfgbridge-new \
    file://mrvl-sd8787.conf \
    file://0001-add-sanity-check-for-missing-NONPLUG_SUPPORT-define.patch \
"

SRC_URI[mfgbundle.md5sum] = "870517accacf6d50e98c5c757e314755"
SRC_URI[mfgbundle.sha256sum] = "de24fb584b9e84ece6bffbdd0058e63ec51cf9b3d490a843ae3d34ecd14b5ccf"

S = "${WORKDIR}/MFG-MF-8787-FC8-SYSKT-${PV}/bin/bridge/bridge_linux_0.1.0.18/"

RDEPENDS_${PN} = "ella-w1-driver"

do_unpack_append() {
    bb.build.exec_func('do_extract_sources', d)
}

do_extract_sources() {
    cd "${WORKDIR}/MFG-MF-8787-FC8-SYSKT-${PV}/bin/bridge/"
    for i in *src.tgz; do
        tar -xzf ${i}
    done
}

do_compile() {
    # compile bridge
    cd "${S}/bridge"

    sed -i \
        -e "s~Load_script=.*~Load_script=\"/opt/${MODULE_NAME}/mfg-tools/load-modules.sh\"~" \
        -e "s~Unload_script=.*~Unload_script=\"/opt/${MODULE_NAME}/mfg-tools/unload-modules.sh\"~" \
        -e "s~fw=.*~fw=\"/lib/firmware/mrvl/mfg/w8787_wlan_SDIO_bt_SDIO.bin\"~" \
        ./bridge_init.conf;
    oe_runmake build CONFIG_MFG_UPDATE=n CC="${CC} ${LDFLAGS}" LD="${LD}" AS="${AS}"
}

do_install() {
    cd ${S}../../FwImage/
    install -d ${D}/lib/firmware/mrvl/mfg
    install -m 0644 w8787_wlan_SDIO_bt_SDIO.bin ${D}/lib/firmware/mrvl/mfg/
    install -m 0644 w8787_wlan_SDIO_bt_UART.bin ${D}/lib/firmware/mrvl/mfg/
    # install bridge bins
    cd ${S}/bin_mfgbridge
    install -d ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 mfgbridge ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0644 bridge_init.conf ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/load-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    install -m 0755 ${WORKDIR}/unload-modules.sh ${D}/opt/${MODULE_NAME}/mfg-tools/
    cd ${WORKDIR}

    # install init script
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${WORKDIR}/ella-w1-mfgbridge \
        ${D}${sysconfdir}/init.d/ella-w1-mfgbridge
    install -m 0755 ${WORKDIR}/ella-w1-mfgbridge-new \
        ${D}${sysconfdir}/init.d/ella-w1-mfgbridge-new

    install -d ${D}${sysconfdir}/modprobe.d/
    install -m 0644 ${WORKDIR}/mrvl-sd8787.conf ${D}${sysconfdir}/modprobe.d/${PN}.conf
}

PACKAGES += "${PN}-firmware ${PN}-bridge"

FILES_${PN}-firmware = "/lib/firmware/mrvl/mfg/*"
FILES_${PN}-bridge = "/opt/${MODULE_NAME}/mfg-tools/*"

FILES_${PN}-dbg += "/opt/${MODULE_NAME}/mfg-tools/.debug/*"

FILES_${PN} = "/opt/${MODULE_NAME}/mfg-tools/license /lib/firmware/mrvl/mfg/license /etc/modprobe.d/* /etc/init.d/*"

RDEPENDS_${PN} += "${PN}-firmware ${PN}-bridge"
