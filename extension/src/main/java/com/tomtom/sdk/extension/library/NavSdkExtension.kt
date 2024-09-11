package com.tomtom.sdk.extension.library

import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder

class NavSdkExtension {

    external fun customizeLocationMarker(color: String, ambient: Double, gamma: Double): Unit

    companion object {

        init {
            ShadowHook.init(
                ConfigBuilder()
                    .setMode(ShadowHook.Mode.UNIQUE)
                    .build()
            )
            System.loadLibrary("NativeExt")
        }
    }
}
