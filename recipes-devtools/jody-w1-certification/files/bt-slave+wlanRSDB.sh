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

module=jody
apdevice="hci"
attDetectWlan=5
msleepDetectTime=400

function closeall
{
    echo "close all..."
    killall l2test &>/dev/null
    killall load-bt-drv.sh  &>/dev/null
    read pid <<< `ps -e|grep brcm_patchram_p`
    if [ -n "$pid" ]
    then
        pid=${pid%% *}
        kill $pid
    fi
    killall iperf3  &>/dev/null
    killall wpa_supplicant &>/dev/null
    rmmod bcmdhd &>/dev/null
}
trap closeall EXIT

closeall

# ================= load the driver =========================
hciconfig |grep $apdevice &>/dev/null
if [ $? -ne 0 ]
then
    ./load-bt-drv.sh | grep "Device setup complete" &>/dev/null
    if [ $? -ne 0 ]
    then
        echo "load $module drivers..."
        exit 1
    fi
fi

# ================= check device presence ===========================
COUNTER=0
while [ $COUNTER -lt $attDetectWlan ]
do
    msleep $msleepDetectTime
    hciconfig |grep $apdevice &>/dev/null
    if [ $? -eq 0 ]
    then
        break
    fi
    let COUNTER+=1
done

if [ $COUNTER -ge $attDetectWlan ]
then
    echo "Device \"$apdevice\" not found..."
    exit 2
fi

# we can load wlan driver now
modprobe jody_w164_03a_rsdb

# ===========================================
echo "Scan \"$module\" master..."

APADDR=$(hcitool -i hci0 scan|grep $module|awk "{print \$1}")
if [ -z "$APADDR" ]
then
    echo "\"$module\" not found. Check master and try again."
    exit 3
fi

# ===========================================
echo "Start bluetooth transmission..."

l2test -i hci0 -O 800 -s $APADDR &
if [ $? -ne 0 ]
then
    echo "Start slave failed."
    exit 4
fi

# sleep 5

./ap-sta.sh RSDB-2G 2442 - 192.168.20.1 RSDB-5G - 192.168.50.5
# sleep 4
iperf3 -s -i5 -p5202&

while [ 1 ]
do
    echo "waiting wi-fi connection..."
    sleep 1
    iwc=`iwconfig &>/dev/stdout`
    echo $iwc |grep RSDB-5G &>/dev/null
    if [ $? -eq 0 ]
    then
        break
    fi
done
echo "connected to RSDB-5G"
sleep 5

iperf3 -c 192.168.50.1 -t300 -i5 -p5201
