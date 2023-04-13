#
# Copyright (C) u-blox
#
# For additional copyright information see the LICENSE file in the root of this meta layer.
#

# Recipes that inheret from this class will have helper functions to check for assumed configurations of
# the enclosed distribution, e.g. to ensure kernel config settings.
# In the end, this allows to check for configuration issues and produce more specific error messages.


# Reads .config of the kernel provider and fails if settings are not set
#
# param: $1 : string of kernel settings which needs to be set separated by spaces
#
# return: 0 : settings specified in param are set
#         "<list>" : list of settings not set
#
# exit: 1 : when kernel configuration is not available
#
ubx_sanitize_kernel_config_is_set () {
    local i;
    local settings="";
    local missing_settings="";

    # normalize settings strings
    for i in ${*}; do
        settings="${settings} CONFIG_${i#CONFIG_}";
    done
    # remove leading space
    settings=${settings# }

    # ensure kernel dir is set, as it is called different for daisy
    if test -z "${STAGING_KERNEL_BUILDDIR}"; then
        # in daisy STAGING_KERNEL_BUILDDIR isn't defined
        STAGING_KERNEL_BUILDDIR="${STAGING_KERNEL_DIR}"
    fi

    # check if .config file is available
    if ! [ -f "${STAGING_KERNEL_BUILDDIR}/.config" ]; then
        echo "ERROR: (ubx_sanitize_kernel_config_is_set) could not find kernel configuration file. Expected in \${STAGING_KERNEL_BUILDDIR}=${STAGING_KERNEL_BUILDDIR}"
        exit 1;
    fi

    local ret=0;
    local tmpmatch=0;

    # check for setting in config file
    for i in ${settings}; do
        if ! tmpmatch=$(grep "$i" "${STAGING_KERNEL_BUILDDIR}/.config"); then
            echo "WARNING: (ubx_sanitize_kernel_config_is_set) could not find '$i' in kernel config"
            missing_settings="${missing_settings} ${i}"
            continue;
        fi;
        if echo "$tmpmatch" | grep "${i} is not set" > /dev/null ; then
            missing_settings="${missing_settings} ${i}"
        fi;
    done
    missing_settings=${missing_settings# };
    for i in ${missing_settings}; do
        echo "ERROR: (ubx_sanitize_kernel_config_is_set) kernel configuration '${i}' is not set when it should be"
        ret=1;
    done

    return $ret;
}
