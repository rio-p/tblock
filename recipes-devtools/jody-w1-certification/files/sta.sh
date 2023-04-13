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
# $1 - SSID to connect as a station
# $2 - PSK for SSID or - if open network
# $3 - ip addr or dhcp
#
# call examples: ./sta.sh ubx-co-testAP5G ubx4bln16 dhcp
#                ./sta.sh ubx-co-testAP5G - 192.168.1.5
#
#
# $4 - SSID2 to connect as a station
# $5 - PSK for SSID2 or - if open network
# $6 - ip addr or dhcp
#                   $0    $1    $2  $3         $4      $5  $6
# call example: ./sta.sh RSDB-5G - 192.168.50.5 RSDB-2G -   192.168.20.5
#

killall wpa_supplicant
sleep 1

PATH_TO_SUPP="/opt/jody-w1/pcie/"

${PATH_TO_SUPP}wpa_supplicant -iwlan0 -Dnl80211 -c/opt/jody-w1/certification/wpa_supplicant.conf -e/var/run/entropy.bin -g/var/run/wpa_wlan0_cmd -B
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 DRIVER interface_create sta0
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run interface_add sta0 /opt/jody-w1/certification/wpa_supplicant_sta.conf nl80211
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 disconnect
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 remove_net all
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 add_net
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 set_net 0 ssid \"$1\"

if [ "$2" = "-" ]
then
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 set_net 0 key_mgmt NONE
else
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 set_net 0 psk \"$2\"
fi

${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 enable_network 0
${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=sta0 select_net 0

if [ "$3" = "dhcp" ]
then
    udhcpc -q -i sta0
else
    ifconfig sta0 $3 netmask 255.255.255.0 up
fi

# start second STA if required
if [ -n "$4" ]
then
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 disconnect
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 remove_net all
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 add_net
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 ssid \"$4\"
    if [ "$5" = "-" ]
    then
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 key_mgmt NONE
    else
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 psk \"$5\"
    fi

    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 enable_network 0
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 select_net 0

    if [ "$6" = "dhcp" ]
    then
        udhcpc -q -i wlan0
    else
        ifconfig wlan0 $6 netmask 255.255.255.0 up
    fi
fi
