#!/bin/sh
#

P3_TOOL_PATH="/opt/vera-p3"
P3_CONF="$P3_TOOL_PATH/vera-p3.conf"
HBOOT="$P3_TOOL_PATH/bin/hboot"
HIF="$P3_TOOL_PATH/bin/hif"
SET_GPIO="p3_gpio_control"

# These parameters should be defined by the user in P3_CONF
unset P3_HOST_INTERFACE
unset P3_REMOTE_MAC
unset P3_FIRMWARE_IMAGE
unset P3_API_SOCKET
unset P3_HBOOT_DEBUG
unset P3_HIF_DEBUG
unset P3_DRIVER

source $P3_CONF

if [ -z "$P3_HOST_INTERFACE" ]; then
    echo "Host interface is not defined. Please configure it in $P3_CONF."
    exit 1
fi

if [ -z "$P3_REMOTE_MAC" ]; then
    echo "MAC address of vera-p3 module is not defined. Please configure it in $P3_CONF."
    exit 1
fi

if [ -z "$P3_API_SOCKET" ]; then
    echo "Location of the vera-p3 api socket is not defined. Please configure it in $P3_CONF."
    exit 1
fi

if [ -z "$P3_DRIVER" ]; then
    echo "Host driver is not defined. Please configure it in $P3_DRIVER."
    exit 1
fi

if [ "$P3_HOST_INTERFACE" != "sdio" ]; then
    if [ -z "$P3_FIRMWARE_IMAGE" ]; then
        echo "Firmware image file of vera-p3 module is not defined. Please configure it in $P3_CONF."
        exit 1
    fi

    if [ -z $P3_HBOOT_DEBUG ]; then
        $HBOOT --iface=$P3_HOST_INTERFACE --remote=$P3_REMOTE_MAC --xretry=0 --load $P3_FIRMWARE_IMAGE --secure --bootreq_timeout 10
    else
        $HBOOT --iface=$P3_HOST_INTERFACE --remote=$P3_REMOTE_MAC --xretry=0 --load $P3_FIRMWARE_IMAGE --secure --bootreq_timeout 10 >>$P3_HBOOT_DEBUG 2>&1
    fi
    RES=$?
    if [ "$RES" != "0" ]; then
        echo "Unable to load $P3_FIRMWARE_IMAGE to vera-p3 ($P3_REMOTE_MAC)."
        echo "$HBOOT exit code: $RES."
        exit $RES
    fi
else
    if type $SET_GPIO > /dev/null 2>&1; then
        $SET_GPIO init
        sleep 1
    fi
    modprobe $P3_DRIVER
    $HBOOT --iface=$P3_HOST_INTERFACE --load $P3_FIRMWARE_IMAGE --secure --bootreq_timeout 10
fi

if [ -z $P3_HIF_DEBUG ]; then
    $HIF --iface $P3_HOST_INTERFACE --remote $P3_REMOTE_MAC --app_sock_path $P3_API_SOCKET &
elif [ -n $P3_UART_CONF ]; then
    P3_UART_CONF="$P3_TOOL_PATH/uart.json"
    $HIF --iface $P3_HOST_INTERFACE --remote $P3_REMOTE_MAC --app_sock_path $P3_API_SOCKET --log_display $P3_FIRMWARE_IMAGE.strings --json $P3_UART_CONF >>$P3_HIF_DEBUG 2>&1 &
else
    $HIF --iface $P3_HOST_INTERFACE --remote $P3_REMOTE_MAC --app_sock_path $P3_API_SOCKET --log_display $P3_FIRMWARE_IMAGE.strings >>$P3_HIF_DEBUG 2>&1 &
fi
