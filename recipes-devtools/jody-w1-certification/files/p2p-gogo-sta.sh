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

#
# parameters:
# $1 - GO1: frequency in MHz
# $2 - GO1: ip addr
# call example: ./p2p-sta.sh 5180 192.168.33.1

# $3 - GO2: frequency in MHz
# $4 - GO2: ip addr
# call example: ./p2p-sta.sh 5180 192.168.33.1 2442 192.168.44.1

# $5 - SSID to connect as a station
# $6 - PSK for SSID or - if open network
# $7 - ip addr or dhcp

# call examples: ./p2p-sta.sh 5180 192.168.33.1 2442 192.168.44.1 RSDB-5G - dhcp
#                ./p2p-sta.sh 5180 192.168.33.1 2442 192.168.44.1 RSDB-5G 12345678 192.168.50.5

attDetectWlan=30
msleepDetectTime=400
p2p_go1="p2p-wlan0"
p2p_go2="p2p-0"

checkDevice()
{
    COUNTER=0
    while [ $COUNTER -lt $attDetectWlan ]
    do
        msleep $msleepDetectTime
        DEV=`ifconfig|grep $1`
        if [ -n "$DEV" ]
        then
            NETDEV=${DEV%% *}
            return 0
        fi
        let COUNTER+=1
    done
    return 1
}

killall wpa_supplicant
sleep 1
rfkill unblock all
rfkill unblock all

PATH_TO_SUPP="/opt/jody-w1/pcie/"
${PATH_TO_SUPP}wpa_supplicant -iwlan0 -Dnl80211 -c/opt/jody-w1/certification/wpa_supplicant.conf -e/var/run/entropy.bin -g/var/run/wpa_wlan0_cmd -B

# start P2P AGO1:
${PATH_TO_SUPP}wpa_cli p2p_group_add freq=$1
checkDevice $p2p_go1
if [ $? -ne 0 ]
then
    echo "Can not create GO interface..."
    exit 2
fi
${PATH_TO_SUPP}wpa_cli -i$NETDEV wps_pbc
ifconfig $NETDEV $2

if [ -n "$3" ]
then
    # start P2P AGO2:
    ${PATH_TO_SUPP}wpa_cli p2p_group_add freq=$3
    checkDevice $p2p_go2
    if [ $? -ne 0 ]
    then
        echo "Can not create GO2 interface..."
        exit 2
    fi
    ${PATH_TO_SUPP}wpa_cli -i$NETDEV wps_pbc
    ifconfig $NETDEV $4
fi

### start STA if required
if [ -n "$5" ]
then
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 disconnect
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 remove_net all
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 add_net
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 ssid \"$5\"
    if [ "$6" = "-" ]
    then
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 key_mgmt NONE
    else
        ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 psk \"$6\"
    fi

    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 enable_network 0
    ${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 select_net 0

    if [ "$7" = "dhcp" ]
    then
        udhcpc -q -i wlan0
    else
        ifconfig wlan0 $7 netmask 255.255.255.0 up
    fi
fi
