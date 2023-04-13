#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

# This class adds a task that copies the driver version file to the driver installation folder.

MODULE_IDENTITY="${MODULE_NAME}-${POSTFIX}"
FILE_PKG_INFO="${MODULE_IDENTITY}-driver_pkg_info"

addtask do_getversion after do_install before do_package

fakeroot do_getversion() {
    echo "FW_VER=${FW_VER}" > ${WORKDIR}/${FILE_PKG_INFO}
    echo "DRIVER_VER=${DRIVER_VER}" >> ${WORKDIR}/${FILE_PKG_INFO}
    if [ "${BT_FW_VER}" ]; then
        echo "BT_FW_VER=${BT_FW_VER}" >> ${WORKDIR}/${FILE_PKG_INFO}
    fi
    install -m 644 ${WORKDIR}/${FILE_PKG_INFO} ${D}${INSTALLDIR}/${FILE_PKG_INFO}
}

FILES_${PN} += "${INSTALLDIR}/${FILE_PKG_INFO}"
