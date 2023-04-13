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
# ssid=emmy
wl=/opt/jody-w1/mfg-tools/wl
# ipAddr=172.22.1.100
attDetectWlan=5
msleepDetectTime=400
apdevice="wlan"
step=1
historyFile=".tx_history"

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

    rmmod bcmdhd &>/dev/null
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

killall -q connmand wpa_supplicant
rfkill unblock all
rmmod bcmdhd &>/dev/null

. $historyFile &>/dev/null

# ================= load the driver =========================
select_jody_w1_model $JODY_W1_MODELS
modprobe ${JODY_W1_SELECTED}-mfg &> /dev/null
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

# ================= basic configurations ===========================
viewStep $step "set device configuration..."

# disable the adapter
$wl down &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl down"
fi

# dis min power consumption mode
$wl mpc 0 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl mpc 0"
fi

# SW transmit power control
$wl phy_txpwrctrl 1 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl phy_txpwrctrl 1"
fi

# change country code here! Get all possible country codes: "wl country list"
$wl country ALL &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl country ..."
fi

# dis watchdog because can hit to run something in phy...
$wl phy_watchdog 0 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl phy_watchdog 0"
fi

viewSuccess

# ================= set channel ===========================
hint="(1-13 | 36-165)"
getInput "   enter TX Channel" "$hint" "wlanChannel" "$wlanChannel_hystory"

wlanChannelBW=20
if [ $wlanChannel -le "13" ]
then
    band=b
    rateSetting=2g_rate
    hint="legacy, HT (l,h)"
else
    band=a
    rateSetting=5g_rate
    hint="legacy, HT, VHT (l,h,v)"
fi

getInput "   enter TX type -" "$hint" "txType" "$txType_hystory"

case $txType in
    "h" )
        if [ "$band" = "a" ]
        then
            hint="(20,40)"
            getInput "   enter channel bandwidth" "$hint" "wlanChannelBW" "$wlanChannelBW"
        fi
        hint="(0-7)"
        rateSetting+=" -h"
    ;;

    "v" )
        hint="(20,40,80)"
        getInput "   enter channel bandwidth" "$hint" "wlanChannelBW" "$wlanChannelBW"
        if [ "$wlanChannelBW" = "20" ]
        then
            hint="(0-8)"
        else
            hint="(0-9)"
        fi
        rateSetting+=" -v"
    ;;

    * )
        if [ "$band" = "b" ]
        then
            hint="\n     DSSS: 1,2,5.5,11\n     OFDM: 6,9,12,18,24,36,48,54"
        else
            hint="\n     OFDM: 6,9,12,18,24,36,48,54"
        fi
        rateSetting+=" -r"
    ;;
esac

getInput "   enter TX DataRate" "$hint" "wlanDataRate" "$wlanDataRate_hystory"

viewStep $step "set TX channel: $wlanChannel"

# set band
$wl band $band &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl band $band"
fi

# set channel and bandwidth
$wl chanspec ${wlanChannel}/${wlanChannelBW} &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl chanspec ${wlanChannel}/${wlanChannelBW}"
fi

viewSuccess

# ================= set data rate ===========================

viewStep $step "set TX data rate: $wlanDataRate"
$wl $rateSetting $wlanDataRate -b ${wlanChannelBW} &>/dev/null

if [ $? -ne 0 ]
then
    myExit $step  "wl $rateSetting $wlanDataRate"
fi

# enable the adapter
$wl up &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl up"
fi

# force the PHY calibration
$wl phy_forcecal 1 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl phy_forcecal 1"
fi

viewSuccess

# ================= set output power ===========================
hint="(dBm)"
getInput "   enter TX output power" "$hint" "wlanOutputPower" "$wlanOutputPower_hystory"

viewStep $step "set TX output power: $wlanOutputPower dBm"

# Set TX output power
$wl txpwr1 -o -d $wlanOutputPower &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl txpwr1 -o -d $wlanOutputPower"
fi

# disable background scan
$wl scansuppress 1 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl scansuppress 1"
fi

viewSuccess

# ================= set core and start sending ===========================
hint="(1=core0, 2=core1)"
getInput "   enter core" "$hint" "core" "$core_hystory"

viewStep $step "start send on core $core"

# select core
$wl txchain $core &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl txchain $core"
fi

# start tx:
$wl pkteng_start 10:20:30:40:50:60 tx 20 2048 0 &>/dev/null
if [ $? -ne 0 ]
then
    myExit $step  "wl pkteng_start 10:20:30:40:50:60 tx 20 2048 0"
fi

viewSuccess

echo -e "\nModule sends packets now. Press Enter to stop sending"
echo "module_hystory=$module">$historyFile
echo "wlanChannel_hystory=$wlanChannel">>$historyFile
echo "txType_hystory=$txType">>$historyFile
echo "wlanChannelBW_hystory=$wlanChannelBW">>$historyFile
echo "wlanOutputPower_hystory=$wlanOutputPower">>$historyFile
echo "wlanDataRate_hystory=$wlanDataRate">>$historyFile
echo "core_hystory=$core">>$historyFile

read qwe

$wl pkteng_stop tx

rmmod bcmdhd &>/dev/null
exit 0

while [ 1 ]
do
    sleep 10
    echo -n "."
done
