# meta-ublox-modules

This layer contains the recipes for drivers and support tools for the u-blox host-based short range radio chips and modules.

Latest recipes with respective driver and firmware versions are:

| Recipe | FW_VER | DRIVER_VER | BT_FW_VER |
| ------ | ------ | ---------- | --------- |
| ella-w1-driver-automotive_14.44.42.p2.bb        | W14.44.42.p2  | M4X14540   |
| ella-w1-driver-industrial_14.66.35.p65.bb       | W14.66.35.p65 | M3X14539   |
| emmy-w1-driver-sdiosdio_15.68.19.p22.bb         | 15.68.19.p22  | C4X15635   | 15.26.19.p22  |
| emmy-w1-driver-sdiouart_15.44.19.p19.bb         | 15.44.19.p19  | C4X15632   | 15.100.19.p19 |
| jody-w1-driver-fmac_4.14.77-2019_0503.bb        | 9.40.115      | 4.14.77    |
| jody-w1-driver-pcie_89359-2019-03-08.bb         | 9.40.112.1    | 1.363.68   |
| jody-w1-driver-sdio_89359-2019-03-08.bb         | 9.40.112.1    | 1.363.68   |
| jody-w1-bt-hcd.bb                               |               |            | 002.002.014.0140 |
| jody-w2-driver-sdio_16.68.10.p33.bb             | 16.68.10.p33  | C4X16651   | 16.26.10.p33 |
| jody-w2-driver-sdiouart_16.68.1.p195-C4X16623.bb | 16.68.1.p195  | C4X16623   | 16.26.1.p195 |
| jody-w3-driver-pcieuart_17.68.0.p137.bb         | 17.68.0.p137  | MXM4X17137 | 17.26.0.p137 |
| jody-w3-driver-sdio_17.68.0.p137.bb         | 17.68.0.p137  | MXM4X17137 | 17.26.0.p137 |
| lily-w1-driver-sdio_14.68.36.p139.bb            | 14.68.36.p139 | C4X14635
| lily-w1-driver-usb_14.68.36.p131.bb             | 14.68.36.p131 | C4X14616
| vera-p3-driver-sdio_2.0.7.bb                    |               | 2.0.8      |
| vera-p3-firmware_2.0.7.bb                       | 2.0.8
| vera-p3-host-tools_2.0.7.bb                     | 2.0.8

## Deliverables Manually Fetching

The deliverables provided by u-blox are not available via an URI.
For integrating those into the yocto build environment manually the usage of the `PREMIRRORS` feature is recommended (local.conf):

```shell
BBPATH = "${TOPDIR}"
BSPDIR := "${@os.path.abspath(os.path.dirname(d.getVar('FILE', True)) + '/../..')}"

PREMIRRORS_prepend = "\
     git://.*/.* file://${BSPDIR}/mirror/ \n \
     ftp://.*/.* file://${BSPDIR}/mirror/ \n \
     http://.*/.* file://${BSPDIR}/mirror/ \n \
     https://.*/.* file://${BSPDIR}/mirror/ \n"
```

After updating the local.conf the deliverables (driver packages) shall be stored in the `./mirror` directory.
The buildsystem will find the archives afterwards.

It would also be possible to copy the deliverables into the download directory, but this is likely to result in several warning messages.


## Enhance BuildEnvironment

The recipes of the meta-ublox-modules layer are used in an automatic environment.
Since the URIs of the sources are not available publicly, the URLs of the sources used in the recipes, need to be filled with a placeholder.
The following excerpt shows a recommended way to setup the environment of the build.

```shell
mkdir ./build -p
source sources/poky/oe-init-build-env ./build/

export LULA_DL_URL="http://company.com/path/to/mirror/" #might fail since this url is just a placeholder
export LULA_DL_PARAMS=""

export BB_ENV_EXTRAWHITE="$BB_ENV_EXTRAWHITE LULA_DL_URL LULA_DL_PARAMS"
```


## Extending build steps

In order to avoid possible build errors with `ubx-mlanutl-native-provider.bbclass`, it is recommended to explicitly limit `COMPATIBLE_MACHINE` to the desired build target(s) in bbappends extending `do_compile()` or `do_install()`.
For example:

```shell
COMPATIBLE_MACHINE = "apalis-tk1|apalis-tk1-b205|apalis-tk1-mainline|apalis-imx6|apalis-imx6-gdp"
```


## Copyright and Licensing

All metadata is licensed under the terms and conditions in LICENSE unless otherwise stated.

Additional files such as source code, patches and binary files included for individual recipes are under the license specified in the respective recipe (`.bb` file) unless otherwise stated.
