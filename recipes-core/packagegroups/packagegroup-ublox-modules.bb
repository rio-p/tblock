#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR = "r15"
SUMMARY = "Provides driver packages for u-blox short-range radio modules"
DESCRIPTION = "Meta group for u-blox radio modules"

inherit packagegroup

PROVIDES = "${PN}"

# Use all modules if not specified otherwise
UBLOX_FEATURES ?= "ella-w1 emmy-w1 lily-w1 vera-p1 jody-w1 jody-w2 jody-w3 vera-p3"

RDEPENDS_${PN} = " \
    ${@bb.utils.contains('UBLOX_FEATURES', 'emmy-w1', 'emmy-w1-driver-sdiouart emmy-w1-driver-sdiosdio emmy-w1-mfg emmy-w1-labtool', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'emmy-w1-sdio', 'emmy-w1-driver-sdiosdio', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'emmy-w1-sdiouart', 'emmy-w1-driver-sdiouart', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'emmy-w1-mfg', 'emmy-w1-mfg emmy-w1-labtool', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'ella-w1', 'ella-w1-driver-automotive ella-w1-driver-industrial ella-w1-mfg', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'ella-w1-automotive', 'ella-w1-driver-automotive', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'ella-w1-industrial', 'ella-w1-driver-industrial', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'ella-w1-mfg', 'ella-w1-mfg', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'lily-w1', 'lily-w1-driver-sdio lily-w1-driver-usb lily-w1-labtool lily-w1-mfg', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'lily-w1-sdio', 'lily-w1-driver-sdio', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'lily-w1-usb', 'lily-w1-driver-usb', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'lily-w1-mfg', 'lily-w1-labtool lily-w1-mfg', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'theo-p1', 'theo-p1-driver-usb', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'vera-p1', 'vera-p1-driver-llc-remote', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w1', 'jody-w1-driver-pcie jody-w1-driver-sdio jody-w1-mfg brcm-patchram-plus jody-w1-bt-hcd jody-w1-certification', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w1-sdio', 'jody-w1-driver-sdio brcm-patchram-plus jody-w1-bt-hcd', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w1-pcie', 'jody-w1-driver-pcie brcm-patchram-plus jody-w1-bt-hcd', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w1-mfg', 'jody-w1-mfg jody-w1-certification', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w1-fmac', 'jody-w1-driver-fmac', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w2', 'jody-w2-driver-sdio jody-w2-driver-sdiouart jody-w2-mfg jody-w2-labtool wapi-ap-app', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w2-sdio', 'jody-w2-driver-sdio', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w2-sdiouart', 'jody-w2-driver-sdiouart', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w2-mfg', 'jody-w2-mfg jody-w2-labtool', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w3', 'jody-w3-driver-pcieuart jody-w3-driver-sdiouart jody-w3-driver-sdio jody-w3-mfg', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w3-pcieuart', 'jody-w3-driver-pcieuart', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w3-sdiouart', 'jody-w3-driver-sdiouart', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'jody-w3-sdio', 'jody-w3-driver-sdio', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'vera-p3', 'vera-p3-host-tools vera-p3-driver-sdio vera-p3-firmware ', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'vera-p3-sdio', 'vera-p3-host-tools vera-p3-driver-sdio vera-p3-firmware', '', d)} \
    ${@bb.utils.contains('UBLOX_FEATURES', 'vera-p3-eth', 'vera-p3-host-tools vera-p3-firmware', '', d)} \
"

do_clean[rdeptask] = "do_clean"
do_cleansstate[rdeptask] = "do_cleansstate"
do_cleanall[rdeptask] = "do_cleanall"
