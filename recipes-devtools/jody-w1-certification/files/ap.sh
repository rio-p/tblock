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
# parameters:
# $1 - AP ssid
# $2 - AP frequency in MHz
# $3 - AP PSK or - if open network
# $4 - AP ip addr
#                   $0    $1     $2  $3    $4
# call example: ./ap.sh RSDB-5G 5180 - 192.168.50.1
#
# $5 - AP2 ssid
# $6 - AP2 frequency in MHz
# $7 - AP2 PSK or - if open network
# $8 - AP2 ip addr
#                   $0    $1     $2  $3    $4         $5     $6  $7 $8
# call example: ./ap.sh RSDB-5G 5180 - 192.168.50.1 RSDB-2G 2437 - 192.168.20.1
#

killall wpa_supplicant
sleep 1

PATH_TO_SUPP="/opt/jody-w1/pcie/"

${PATH_TO_SUPP}wpa_supplicant -iwlan0 -Dnl80211 -c/opt/jody-w1/certification/wpa_supplicant.conf -e/var/run/entropy.bin -g/var/run/wpa_wlan0_cmd -B
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 DRIVER interface_create bcm0
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run interface_add bcm0 /opt/jody-w1/certification/wpa_supplicant_ap.conf nl80211
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 remove_net all
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 add_net
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 set_net 0 ssid \"$1\"
if [ "$3" = "-" ]
then
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 set_net 0 key_mgmt NONE
else
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 set_net 0 psk \"$3\"
fi

${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 set_net 0 frequency $2
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 set_net 0 mode 2
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm0 select_net 0
ifconfig bcm0 $4 netmask 255.255.255.0 up

# start second AP if required
if [ -n "$5" ]
then
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 DRIVER interface_create bcm1
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run interface_add bcm1 /opt/jody-w1/certification/wpa_supplicant_ap.conf nl80211
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 remove_net all
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 add_net
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 set_net 0 ssid \"$5\"
    if [ "$7" = "-" ]
    then
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 set_net 0 key_mgmt NONE
    else
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 set_net 0 psk \"$7\"
    fi

    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 set_net 0 frequency $6
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 set_net 0 mode 2
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=bcm1 select_net 0
    ifconfig bcm1 $8 netmask 255.255.255.0 up
fi
