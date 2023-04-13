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

exit_with_error() { echo $@ 1>&2; exit 1; }

JODY_W1_FW_PATH=/lib/firmware/brcm
JODY_W1_MODELS=$(find ${JODY_W1_FW_PATH} -name *.nvram -type f \
    -exec basename {} .nvram \; 2> /dev/null | sort)
[ -n "$JODY_W1_MODELS" ] || exit_with_error "No JODY-W1 models found."
JODY_W1_SELECTED=

# module=
# ssid=CMW-AP
ssid="jody"
wl=/opt/jody-w1/mfg-tools/wl
PATH_TO_SUPP="/opt/jody-w1/pcie/"
ipAddr=172.22.1.100
ipAddrAP=172.22.1.10
attDetectWlan=5
msleepDetectTime=400
apdevice="wlan"
step=1
historyFile=".sta_history"

# colour codes
NORM="\e[0m"
BOLD="\e[1m"
INVS="\e[7m"
BLINK="\e[5m"
LRED="\e[1;31m"
LGREEN="\e[1;32m"
GREEN="\e[0;32;40m"
ULIN="\e[4m"

# defaults

# functions

viewSuccess () {
    echo -e "\r\t\t\t\t\t${GREEN}[SUCCESS]${NORM}"
}

viewStep () {
    echo -n "${1}. ${2}"
    let step="$step+1"
}

myExit () {
    if [ "$1" = "0" ]
    then
        echo -e "\n${LGREEN}*** STARTED ***  ${NORM}"
    else
        echo -e "\n\n${LRED}*** FAIL *** ${2}${NORM}\n"
    fi

    sleep 2
    exit $1
}

getInput () { # prompt message, hint, varName, history
    echo -ne "$1 ${2}: $4 "

    read value
    if [ -z $value ]
    then
        eval "$3=$4"
    else
        eval "$3=$value"
    fi
}

select_jody_w1_model() {
    local i

    echo "Listing available JODY-W1 models:"
    for i in $(seq $#); do
        echo "  "$(eval echo "$i\) \${$i}" | tr '[:lower:]' '[:upper:]')
    done

    while read -p "Select model: " -r i; do
        if [ $i -ge 1 -a $i -le $# ]; then
            break;
        fi 2> /dev/null
    done

    JODY_W1_SELECTED=$(eval echo \${$i})
}

. $historyFile &>/dev/null

killall -q connmand wpa_supplicant
rfkill unblock all
rmmod bcmdhd &>/dev/null

# ================= load the driver =========================
select_jody_w1_model $JODY_W1_MODELS
modprobe ${JODY_W1_SELECTED} &> /dev/null
if [ $? -ne 0 ]
then
    myExit $step  "load $module drivers..."
fi

viewSuccess

# ================= check wlan presence ===========================
viewStep $step "check WLAN device presence..."

COUNTER=0
while [ $COUNTER -lt $attDetectWlan ]
do
    msleep $msleepDetectTime
    ifconfig -a|grep $apdevice &>/dev/null
    if [ $? -eq 0 ]
    then
        break
    fi
    let COUNTER+=1
done

if [ $COUNTER -ge $attDetectWlan ]
then
    myExit $step  "WLAN device \"$apdevice\" not found..."
fi

viewSuccess

# ================= set capabilities ===========================
hint="(20,40,80)"
getInput "   enter channel bandwidth" "$hint" "wlanChannelBW" "$wlanChannelBW_hystory"

viewStep $step "set bandwidth capability..."

case "$wlanChannelBW" in
    20) bwcap=1 ;;
    40) bwcap=3 ;;
    * ) bwcap=7 ;;
esac

$wl bw_cap a $bwcap &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl bw_cap a $bwcap..."
fi

# change country code here! Get all possible country codes: "wl country list"
$wl country ALL &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl country ..."
fi

ifconfig wlan0 up &>/dev/null
if [ $COUNTER -ge $attDetectWlan ]
then
  myExit $step  "can not up ..."
fi

viewSuccess

# ================= start wpa wpa_supplicant ===========================
viewStep $step "start wpa_supplicant..."

${PATH_TO_SUPP}wpa_supplicant -iwlan0 -Dnl80211 -cwpa_supplicant.conf -e/var/run/entropy.bin -g/var/run/wpa_wlan0_cmd -B &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step "start wpa_supplicant"
fi

viewSuccess

# =================  ===========================
viewStep $step "remove existing networks..."

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 disconnect`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "wlan0 disconnect"
fi

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 remove_net all`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "wlan0 remove_net all"
fi
viewSuccess

# ================= connect to the AP ===========================
viewStep $step "connect to the AP \"jody\"..."

${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 add_net &>/dev/null
if [ $? -ne 0 ]
then
     myExit $step "wlan0 add_net"
fi

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 ssid '"jody"'`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "set ssid: jody"
fi

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 set_net 0 key_mgmt NONE`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "key_mgmt NONE"
fi

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 enable_network 0`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "wlan0 enable_network 0"
fi

RETMSG=`${PATH_TO_SUPP}wpa_cli -iwpa_wlan0_cmd -p /var/run IFNAME=wlan0 select_net 0`
if [ "$RETMSG" != "OK" ]
then
    myExit $step "wlan0 select_net 0"
fi

viewSuccess

echo "module_hystory=$module">$historyFile
# echo "frequency_hystory=$frequency">>$historyFile
echo "wlanChannelBW_hystory=$wlanChannelBW">>$historyFile
# echo "core_hystory=$core">>$historyFile

# ================= set ip addr ===========================
viewStep $step "set IP address..."

ifconfig wlan0 $ipAddr &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "Set IP address..."
fi
viewSuccess

iperf3 -u -c $ipAddrAP -i3 -t30000 -b20M
