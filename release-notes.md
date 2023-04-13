# Release notes

## JODY-W1

### jody-w1-bt-hcd

JODY-W1 Bluetooth firmware version 002.002.014.0140

Known issues and limitations:
  * If two WBS eSCO connections are created, need to use packet type 2-EV3 or higher.
    Any lower packet type/size will result in degradation of eSCO/Voice quality.
    This limitation will not be addressed due to limited processing time and air time.
    EV3 can be used for one WBS eSCO connections.

Changes (since Rev_0135):
  * Rev_0140: Release WBS buffers during eSCO disconnection if WBS is disabled.
  * Rev_0139: Restore the PCM time slot after repeater mode is disabled
  * Rev_0138: Fix secure connection MIC errors
  * Rev_0137: Change default value of A2DP FlowOff check timer
  * Rev_0136: Pairing failure caused by LE version exchange

### jody-w1-driver-fmac (v4.14.77-2019_0503)

This is Cypress's Linux brcmfmac driver and firmware support package.
Brcmfmac is an open-source driver project.

Supported Features:
 * Concurrent APSTA
 * P2P
 * Out-of-band (OOB) interrupt
 * CLM download (43455/4343w/4354/4356/43012/89342/89359/4373)
 * Wake on Wireless LAN
 * Voice enterprise (43455)
 * PMF
 * WPA3 (43455)
 * RSDB (89342/89359)
 * Thermal throttling (4343w)
 * Fast roaming (89342)

FMAC Driver Changes:
 * CLM download support/fix (0001, 0009, 0032-0033)
 * 4373 support (0002, 0041, 0056, 0059, 0067-0069, 0071, 0074)
 * Device tree related changes (0003)
 * General bug fixes (0004, 0006, 0010, 0045, 0066, 0084, 0090, 0094, 0095)
 * 43012 support (0005, 0007, 0012-0013, 0017, 0043, 0058, 0093)
 * 43455 support (0008, 0087, 0106)
 * Throughput enhancement (0011, 0019, 0025-0026, 0030, 0044, 0072)
 * Fast roaming support (0014, 0028, 0051-0054, 0098-0099, 0103)
 * 43428 support (0015)
 * AP isolation support (0016)
 * Wake on Wireless LAN fix (0018, 0021-0022, 0070, 0076, 0086)
 * EAP restriction setting (0020)
 * 89342 support (0023)
 * Fcmode 2 support (0024, 0027)
 * General bug fixes (0029, 0031, 0036-0038)
 * WFA certification fixes (0034-0035, 0039-0040, 0042, 0057, 0096-0097, 0101-0102, 0110-0111)
 * RSDB (0045-0049, 0079-0080)
 * 89342/89359 support (0050, 0073, 0107)
 * 4356 support (0060, 0085)
 * WPA3-personal (0061-0065, 0077)
 * Power saving changes (0075, 0100, 0104-0105)
 * USB support/fixes (0078, 0081-0083, 0088-0089)
 * Fully preemptive kernel support (0091)
 * Code clean up (0092)
 * Security fixes (0108, 0109)

## JODY-W2

### jody-w2-driver-sdio (W16.68.10.p33-16.26.10.p33-C4X16651)

WLAN SOC/RF chipset: 88W8987 QFN 2-ANT A1

Supported features:
 * Wireless Client 802.11a/b/g/n/ac
 * Mobile AP 802.11a/b/g/n/ac
 * WPA3
 * Wi-Fi Direct / P2P
 * Wi-Fi Display /Miracast
 * Wi-Fi Aware / Neighbor Awareness Network (NAN)
 * Wi-Fi Agile Multiband Operation (MBO)
 * EU conformance (EU Adaptivity 2.4 and 5 GHz)
 * Wi-Fi Location Service (WLS)
 * 802.1AS -- Time Sync
 * Voice over Wi-Fi Personal
 * Simultaneous Modes
 * Dynamic Rapid Channel Switch (DRCS)
 * Bluetooth Classic / 2.1 / 3.0 / 4.0 / 4.1
 * Bluetooth LE 4.0 / 4.1 / 4.2 / 5.0
 * Bluetooth + WLAN Coexistence

WLAN bug fixes / feature enchancements:
 * WIFI output power reduced by 20dB in 5GHz

BT/BLE bug fixes / feature enchancements:
 * DUT stops receiving ADV packets when performed LE SCAN with APCF feature
   enabled.

WLAN known issues:
 * P2P connection stability is being tuned when using host MLME.

### jody-w2-driver-sdiouart (W16.68.1.p195-16.26.1.p195-C4X16623)

WLAN SOC/RF chipset: 88W8987 QFN 2-ANT A1

Supported features:
  * Wireless Client 802.11a/b/g/n/ac
  * Mobile AP 802.11a/b/g/n/ac
  * Wi-Fi Direct / P2P
  * Wi-Fi Display /Miracast
  * Wi-Fi Aware / Neighbor Awareness Network (NAN)
  * Wi-Fi Agile Multiband Operation (MBO)
  * EU conformance (EU Adaptivity 2.4 and 5 GHz)
  * Wi-Fi Location Service (WLS)
  * 802.1AS - Time Sync
  * Voice over Wi-Fi Personal
  * Simultaneous Modes
  * Dynamic Rapid Channel Switch (DRCS)
  * Bluetooth Classic / 2.1 / 3.0 / 4.0 / 4.1
  * Bluetooth LE 4.0 / 4.1 / 4.2 / 5.0
  * Bluetooth + WLAN Coexistence

WLAN bug fixes / feature enchancements:
  * STA connection operation with DUT in Mobile AP mode configured with
    latest WPA2 security improvements has been refined

BT/BLE bug fixes / feature enchancements:
  * Sniff/Unsniff operation during Power-Save enabled scenarios has been
    rectified.
  * Out-Band Independent Reset Response by triggering GPIO signal has been
    improved

WLAN known issues:
  * Throughput is being optimized for STA and MMH modes
  * Pre-Cert issues in NAN feature related to few specific use cases are
    being debugged
  * Few isolated issues in 11p functionality are being debugged.

BT/BLE known issues:
  * Audio quality is being optimized during simultaneous A2DP Sink and BLE
    Scan operation with A2DP controller in Master mode
  * BLE Throughput is being optimized for certain connection intervals

Coex known issues:
  * WLAN throughput during BT Inquiry or Paging Scan is being optimized

## JODY-W3

### jody-w3-mfg (2.0.0.72-A0-17.80.200.p135)

This package includes the manufacturing firmware and the device drivers as well
as the bridge and Labtool applications for the 9098 (A0 only) device.

New features:
  * Supports calibration and verification of ePA (external PA) designs.
  * Supports calibration and verification of iPA (internal PA) & ePA (external PA) mixed mode designs.

Notes:
  * Boards calibrated using MFG releases older than p135 may show transmit
    power inaccuracy. Marvell strongly recommends to re-calibrate the
    boards using MFG release p135.
  * This is a preliminary MFG release for 9098 A0 device with limited
    testing. The RF performance has not been optimized.


### jody-w3-driver-pcieuart (17.68.0.p137-MXM4X17137)

Default to A0 firmware.

Supported features:
  * Wireless Client 802.11a/b/g/n/ac/ax
  * Mobile AP 802.11a/b/g/n/ac/ax
  * Wi-Fi Direct
  * Wi-Fi Location Service (WLS)
  * 802.1AS - Time Sync
  * Wi-Fi Aware / Neighbor Awareness Network (NAN)
  * Channel / Spectrum Reporting
  * Simultaneous Modes (Single MAC, Single Band, Single Channel)
  * Concurrent Dual Wifi Mode (Dual MAC, Dual Band, Dual Channel)
  * Bluetooth
  * Bluetooth LE
  * Bluetooth + WLAN Coexistence

WLAN feature enchancements:
  * Firmware recovery scheme is enabled
  * Band Steering feature is enabled and under optimization
  * Updated missing driver-load (insmod) parameters in driver README
  * Enabled method to set MAX STA count for APUT at chip level

WLAN bug fixes 
  * P2P find failed while P2P connection is established
  * P2P interface doesn't send QoS-Null data frame during STA scan
  * Country code can’t be configured through iw reg set command
  * STA disconnected from a router RX501NC after connection
  * STA only scan AP at CH1 after P2P connection is established
  * STA throughput improvement with AP WXR-1900DHP
  * MMH throughput improvement for 5 Ex-STA case
  * STA traffic stopped in case of p2p connection

BT/BLE bug fixes
  *  End signature sync up timeout in uartfwloader after BT firmware is downloaded
  *  Key Negotiation of Bluetooth (KNOB) Attack

WLAN known issues:
  * Throughput for STA mode, Mobile AP and CDW mode is under optimization
  * 11ax mode is being optimized including HE Mode Throughput and UL-OFDMA
  * WPA3 Mode Throughput is being optimized
  * STA gets disconnected from AP using mmlan0 interface after getting connected in power save mode
  * 802.11r Fast Transition has disconnection issue sometimes when STA is trying to connect to external AP with Fast Transition enabled.
  * P2P connection establishment failure is observed sometimes and is being addressed.
  * SoC Spectral Analysis Unit (SSU) available in 2.4 GHz only
  * No response from STA to Null frame from AP(NEC Aterm WR8700N)
  * 11ax/11ac dynamic mode switching is being optimized
  * Power consumption under optimization
  * NAN feature is under optimization

BT/BLE known issues:
  * LE scan performance with varying advertisement data length under optimization
  * LE Extended Advertising mode is being optimized
  * BLE Scan performance in the presence of BT Activity (Inquiry/Conn/Sniff) is being optimized
  * BT firmware downloading time is being improved

Coex known issues:
  * Running STA / P2P traffic in Radio-1 in the presence of Bluetooth Coex is
    being optimized
  * Throughput in the presence of Bluetooth Inquiry Scan is being optimized


### jody-w3-driver-sdio (17.26.0.p137-MXM4X17137)

WLAN Features
  * Wireless Client Features: 802.11 a/b/g/d/e/i/w/n/ac/ax/k/v/u/z
  * Mobile AP Features: 802.11 a/b/g/d/e/i/w/n/ac/ax
  * Wi-Fi Direct / P2P Features: Max Client Support - Up to 8 Devices
  * Wi-Fi Location Service (WLS)
  * 802.1AS - Time Sync
  * Wi-Fi Aware / Neighbor Awareness Network (NAN) - R1 Features
  * Wi-Fi Aware / Neighbor Awareness Network (NAN) – R2 Features
  * EU Conformance Tests
  * Simultaneous Modes:
    * MMH Simultaneous Operation
    * P2P Simultaneous Operation
    * AP + STA
    * AP + AP
    * P2P + STA
    * P2P + MMH
    * P2P-GO + P2P-GC
    * AP + AP + STA
    * STA + MMH + P2P
  * Channel / Spectrum Reporting
  * Simultaneous Modes [Single MAC | Single Band | Single Channel]
  * Concurrent Dual WiFi (CDW) Mode [Dual MAC | Dual Band | Dual Channel]

Bluetooth Features
  * Bluetooth Classic
  * BLE
  * Bluetooth + WLAN Coexistence
  *


## VERA-P3

### vera-p3-driver-sdio (2.0.8-2019-09-27)

HIF
  * GNSS uart is now configurable via hif -j –json option
    * hif application now supports a .json hif config file (via hif --json or ubxboard.py --hif_config)
    * Example file available under: scripts/sample_hif_config.json
  * Field testing: Change rssi logging to rcpi logging 
  *  hif tool documentation updates

HBOOT
  * fix degraded performance
  * discover improvements
    * Discover list is now sorted
    * OTP ID can now be reported (new hboot option --discover_otp_id)
    * JSON output support (new hboot option --discover_json)
    * See more in hboot –help
    * Warning: any scripts that screen scrape discover output need to be updated. JSON output mode should be preferred in scripting

UBX-P3 SPI HIF (alpha/unstable, support is only present in _dev packages)
  * common SPI boot/hif kernel module
  * kernel module: move GPIO config to device tree
  * fix SPI boot instabilities
  * fix SPI HIF errors while gnss is running

HIF_TOOL
  * fix test-rx reports 'Alternating-both' option as invalid

MFG
  * OTP page lock support
  * OTP error reporting
  * Internal packet generator fixes and enhancements
