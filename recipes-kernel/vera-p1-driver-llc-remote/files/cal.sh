#!/bin/sh
#
# calibration for VERA-P1 module
#

llc="/opt/vera-p1/llc/llc"

## IQ Calibration
#~ echo Applying IQ Calibration
#~ $llc chconfig -s -c178 -ra -a2
#~ sleep 1 # a small delay is required before running the ‘cal’ command
#~ $llc cal
#~ $llc chconfig -x -c178 -ra

# Temperature Calibration
echo Applying Temperature Calibration
$llc powerdet 1 99999 0 0 99999 0 0 99999 0 0 99999 0 0 0 0 0
$llc temp cfg 2 112 113 0.0707372 -0.2546940 0.0725834 -0.7115092 105 20

# RSSI Calibration
echo Applying RSSI Calibration
$llc rssical 1 3199 -319032 -320973 -326314 -327447 -329794 -330046 -333024 -331686 -334558

# Acq Thresholds
echo Applying Acq Thresholds
$llc reg w 1 8 0x70000
$llc reg w 1 9 0x200000
