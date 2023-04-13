#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

PR ?= "r2"
PR_append = "+ubxmu1"

FILESEXTRAPATHS_prepend := "${THISDIR}/wpa-supplicant:"

SRC_URI_append += " \
    file://defconfig \
"
