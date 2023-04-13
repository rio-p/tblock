#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

addtask fix_module_symvers before do_install after do_compile_kernelmodules

# Workaround a bug in fido, that exported symbols are not updated in the staging
# area for kernel module recipes.
do_fix_module_symvers[doc] = "Workaround a bug introduced in fido and update list of exported symbols"
do_fix_module_symvers() {
    if [ -n "${KBUILD_OUTPUT}" -a \
         -n "${STAGING_KERNEL_BUILDDIR}" ]
    then
        cp -f ${KBUILD_OUTPUT}/Module.symvers ${STAGING_KERNEL_BUILDDIR}
    fi
}
