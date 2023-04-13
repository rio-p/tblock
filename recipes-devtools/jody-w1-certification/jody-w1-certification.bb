#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r1"
SUMMARY = "JODY-W1 certification scripts"

LICENSE = "u-blox-open-source"
LIC_FILES_CHKSUM = " \
    file://${WORKDIR}/ap.sh;beginline=2;endline=23;md5=f260a9c3da316d35f18a260b27de810c \
"

PROVIDES = "${PN}"

RDEPENDS_${PN} = "bash"

INSTALLDIR = "/opt/jody-w1/certification"

SRC_URI = " \
    file://ble-scan.sh \
    file://ble-scan-cmd.sh \
    file://bt-master.sh \
    file://bt-slave.sh \
    file://load-bt-drv.sh \
    file://load-wlan-drv.sh \
    file://load-wlan-mfgdrv.sh \
    file://rx.sh \
    file://setDUTmodeBT.sh \
    file://tx.sh \
    file://tx-rsdb.sh \
    file://wlan-ap.sh \
    file://wlan-rx-blocking.sh \
    file://wlan-sta.sh \
    file://wpa_supplicant_ap.conf \
    file://wpa_supplicant.conf \
    file://wpa_supplicant_sta.conf \
    file://ap.sh \
    file://ap-sta.sh \
    file://sta.sh \
    file://p2p-sta.sh \
    file://p2p-gogo-sta.sh \
    file://bt-master+wlanRSDB.sh \
    file://bt-slave+wlanRSDB.sh \
    file://load-bt-drv-uart.sh \
    file://wlan-ap-dfs.sh \
    file://wlan-sta-dfs.sh \
"

do_install() {
    local f

    install -d ${D}/${INSTALLDIR}
    for f in ${WORKDIR}/*.sh; do
        install -m 755 $f ${D}/${INSTALLDIR}
    done
    for f in ${WORKDIR}/*.conf; do
        install -m 644 $f ${D}/${INSTALLDIR}
    done
}

COMPATIBLE_MACHINE_apalis-tk1 = "apalis-tk1"

FILES_${PN} = "${INSTALLDIR}/*"
