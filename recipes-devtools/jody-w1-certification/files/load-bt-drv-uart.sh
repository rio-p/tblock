#!/bin/sh
#
# Copyright (C) u-blox
#
# u-blox reserves all rights in this deliverable (documentation, software, etc.,
# hereafter "Deliverable").
#
# u-blox grants you the right to use, copy, modify and distribute the Deliverable
# provided hereunder for any purpose without fee, provided this entire notice is
# included in all copies of any software which is or includes a copy or
# modification of this software and in all copies of the supporting documentation
# for such software.
#
# THIS DELIVERABLE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
# WARRANTY. IN PARTICULAR, NEITHER THE AUTHOR NOR U-BLOX MAKES ANY REPRESENTATION
# OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY OF THIS DELIVERABLE OR
# ITS FITNESS FOR ANY PARTICULAR PURPOSE.
#
# In case you provide us a feedback or make a contribution in the form of a
# further development of the Deliverable ("Contribution"), u-blox will have the
# same rights as granted to you, namely to use, copy, modify and distribute the
# Contribution provided to us for any purpose without fee.
#

#
### load driver and initialize BT
###   param1: -
###   param2: -
#
### compatibility: +bash; +ash; -dash
#
### dependencies:
###   mlanutl  -
###   ifconfig -
###
#
### error/exit codes:
###   0 - no errors
###   1 -

# usbport=`ls -1 /dev|grep ttyUSB`
# if [ -z $usbport ]
# then
#   echo "no usb serial port detected. Check connections."
#   exit 1
# fi

killall hciattach &>/dev/null

usbport="/dev/ttyTHS1"
# baudrate definition
# 7layers requires 115200 here
baudrate="3000000"

firmware="/lib/firmware/brcm/"
firmware+=`ls -1 ${firmware}|grep .hcd`
brcm_patchram_plus --no2bytes --tosleep 50000 --baudrate $baudrate --use_baudrate_for_download --patchram $firmware $usbport &>/dev/null

modprobe hci_uart
hciattach $usbport any $baudrate flow
hciconfig hci0 up piscan

sleep 1
