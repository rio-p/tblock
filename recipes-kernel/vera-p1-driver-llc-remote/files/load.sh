#!/bin/sh
#
# load firmware and driver for VERA-P1 module
#

dfu-util -d 1fc9:0102 -R -D /lib/firmware/cohda/FastBootSpiUds.usb
/opt/vera-p1/uds-loader -v 0x1fc9 -p 0x0123 -f /lib/firmware/cohda/SDRMK5Firmware.bin
modprobe cw-llc
ip link set cw-llc0 up
ip link set cw-llc1 up
/opt/vera-p1/cal.sh
