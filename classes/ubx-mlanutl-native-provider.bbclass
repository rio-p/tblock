#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

BBCLASSEXTEND += "native"

SUMMARY_class-native  = "mlanutl tool for Marvell based proprietary firmware and driver"

# we want to depend on the same version of the native package
DEPENDS += "${PN}-${PV}-native"

PACKAGES_class-native := ""
DEPENDS_class-native := ""
RDEPENDS_class-native := ""
PROVIDES_class-native := "${PN}-${PV}-native"

#
# remove unwanted inheritance
#
python () {
    pn = d.getVar("PN", True)

    if not pn.endswith("-native"):
        return

    # remove RDEPENDS for native packages since it depends on firmware and
    # tools packages, which it has inherited from the main pkg.
    d.delVar("RDEPENDS_"+pn);
    d.delVar("DEPENDS_"+pn);
    d.delVar("RPROVIDES_"+pn);

    # Incomplete implementation, reduce to default tasks in a more general way
    bb.build.deltask("do_make_scripts",d)
    bb.build.deltask("do_generate_txpower_bins",d)
    bb.build.deltask("do_patch_txpower_dir",d)
    bb.build.deltask("do_patch_txpowertbl_download_notification",d)
    bb.build.deltask("do_patch_world_domain",d)
    bb.build.deltask("do_compile_qa_ubx",d)
    bb.build.deltask("do_install_txpower_bin",d)
}

do_compile_class-native() {
    unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
    cd ${S}/wlan_src
    oe_runmake mapp/mlanutl
}

do_install_class-native() {
    # Install mlanutl
    cd ${S}/wlan_src/mapp/mlanutl
    install -d ${D}${bindir}/
    install -m 0755 mlanutl ${D}${bindir}/mlanutl-${MODULE_IDENTITY}-native
}
