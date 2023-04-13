#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

# Enable this bbappend if plugins sources are available to you, by unmasking it
# in conf/layer.conf

PA = "aarch64"

DEPENDS_append_aarch64 = " gpsd libpcap"

PLUGINS_SRC = "NXP_V2X_LLC_Remote_${PV}_plugins_src"

SRC_URI_remove_aarch64 = " \
    file://${PV}/0001-fix-compilation-issues.patch \
    file://${PV}/0005-enable-plugin-binaries-for-aarch64.patch \
"
SRC_URI_append_aarch64 = " \
    ${LULA_DL_URL}/${PLUGINS_SRC}_Confidential.tar.gz${LULA_DL_PARAMS};name=plugins \
    file://${PV}-${PA}/0001-fix-compilation-issues.patch \
"
SRC_URI[plugins.md5sum] = "1676f7ac186fdbd1eeec4ac55ddddd29"
SRC_URI[plugins.sha256sum] = "a98e7f806442b28dc884a125d63ed734f3496a1209e8e8c3584fedb8399854fa"

do_unpack_append_aarch64() {
    bb.build.exec_func('do_overwrite_sources_aarch64', d)
}

do_overwrite_sources_aarch64() {
    cp -fr ${WORKDIR}/${PLUGINS_SRC}/* ${S}/cohda/app/llc
}
