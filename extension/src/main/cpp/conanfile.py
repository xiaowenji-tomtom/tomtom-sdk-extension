# Copyright (C) 2022 TomTom NV. All rights reserved.
#
# This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
# used for internal evaluation purposes or commercial use strictly subject to separate
# license agreement between you and TomTom NV. If you are the licensee, you are only permitted
# to use this software in accordance with the terms of your license agreement. If you are
# not the licensee, you are not authorized to use this software in any manner and should
# immediately return or destroy it.

from conans import ConanFile, CMake, python_requires

lockfile = python_requires("conan-lockfile/1.9.1@tomtom/stable")


class SdkExtension(ConanFile):
    name = "sdk-extnsion"
    description = "Conan.io recipe for GoSDK developer app"
    license = "commercial"
    generators = "cmake", "cmake_find_package"
    keep_imports = True

    requires = (
        "mapdisplay-engine/[>=2.3.3]@tomtom/stable",
    )

    build_requires = (
        "cmake-modules/[>=1.0.0]@tomtom/stable",
    )

    settings = "os", "compiler", "build_type", "arch", "toolchain"

    options = {}

    # default_options = {
    #     "navigation-drivingassistance-common:with_protobufcomms": True,
    #     "vehiclehorizon:with_protobufcomms": True,
    # }

    # exports_sources = ["developer-app/*",
    #                    "CMakeLists.txt",
    #                    "local-navtile-service/*",
    #                    "misc/cacert.pem"]

    def requirements(self):
        lockfile.load(self)

    def _configure_cmake(self):
        cmake = CMake(self)

        if not self.options["boost"].shared:
            cmake.definitions["Boost_USE_STATIC_LIBS"] = "ON"

        cmake.configure()

        return cmake

    def build(self):
        cmake = self._configure_cmake()
        cmake.build()
        cmake.install()
