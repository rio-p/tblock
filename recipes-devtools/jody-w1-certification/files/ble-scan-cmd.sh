#!/bin/bash
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

hciconfig hci0 up

# enable the BLE advertising:
hcitool -i hci0 cmd 0x08 0x0006 00 08 00 08 00 00 00 00 00 00 00 00 00 07 00
hcitool -i hci0 cmd 0x08 0x000A 01

# disable the BLE advertising:
#hcitool -i hci0 cmd 0x08 0x000A 00

# set the scanning interval:
hcitool -i hci0 cmd 0x08 0x000B 00 00 08 0x10 0x00 0x00 0x00

# start the scanning
hcitool lescan&

COUNTER=0
while [ 1 ]
do
    sleep 2
    echo "$COUNTER"
    let COUNTER+=1
done
