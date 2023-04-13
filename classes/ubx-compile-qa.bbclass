#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

addtask compile_qa_ubx before do_install after do_compile

do_compile_qa_ubx[doc] = "Verify kernel objects"
do_compile_qa_ubx() {
    check_missing_symbols
}

check_missing_symbols() {
    local module
    local symbol
    # Module.symvers-es local to inheriting recipe
    local symvers=$(find ${S} -name Module.symvers -type f -size +0)
    local fail=0

    set +e
    find "${S}" -name *.ko -type f | \
        while read module; do
            for symbol in $(${NM} -u $module | sed -e 's/^.*U //g'); do
                # Fail if we do not find current symbol in either
                # Module.symvers or System.map
                grep -q "${symbol}" \
                    "${STAGING_KERNEL_BUILDDIR}/Module.symvers" \
                    "${STAGING_KERNEL_BUILDDIR}/"System.map* \
                    "${symvers}"
                if [ $? -ne 0 ]; then
                    bbnote "\"$module\":\"${symbol}\" not found"
                    fail=1;
                fi
            done
        done
    set -e
    if [ ${fail} -ne 0 ]; then
        die "Unsatisfied symbols in kernel modules \n you might want to rerun \"bitbake virtual/kernel -c do_shared_workdir -f\"."
    fi
}