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

attDetectWlan=5
msleepDetectTime=400
apdevice="hci"
step=1

# colour codes
NORM="\e[0m"
BOLD="\e[1m"
INVS="\e[7m"
BLINK="\e[5m"
LRED="\e[1;31m"
LGREEN="\e[1;32m"
GREEN="\e[0;32;40m"
ULIN="\e[4m"

viewSuccess () {
    echo -e "\r\t\t\t\t\t${GREEN}[SUCCESS]${NORM}"
}

viewStep () {
    echo -n "${1}. ${2}"
    let step="$step+1"
    # return $step
}

myExit () {
    if [ "$1" = "0" ]
    then
        echo -e "\n${LGREEN}*** STARTED ***  ${NORM}"
    else
        echo -e "\n\n${LRED}*** FAIL *** ${2}${NORM}\n"
    fi

    sleep 4
    exit $1
}

# 1 ================= load the driver =========================
viewStep $step "load $module drivers..."

hciconfig |grep $apdevice &>/dev/null
if [ $? -ne 0 ]
then
    ./load-bt-drv.sh | grep "Device setup complete" &>/dev/null
    if [ $? -ne 0 ]
    then
        myExit $step  "load $module drivers..."
    fi
fi

viewSuccess

# 2 ================= check device presence ===========================
viewStep $step "check device presence..."

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
    myExit $step  "Device \"$apdevice\" not found..."
fi

viewSuccess

# 3 ===========================================
viewStep $step "Scan \"$module\" master..."

APADDR=$(hcitool -i hci0 scan|grep $module|awk "{print \$1}")
if [ -z "$APADDR" ]
then
    myExit $step  "\"$module\" not found. Check master and try again."
fi

viewSuccess

# 4 ===========================================
viewStep $step "Start bluetooth transmission..."

l2test -i hci0 -O 800 -s $APADDR &
pid=$!

if [ $? -ne 0 ]
then
    myExit $step  "Start slave failed."
fi

viewSuccess

echo -e "[$pid] press Enter to stop transmission "
read qwe

kill $pid

exit 0
