package com.tomtom.sdk.extension.library

import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder

class NavSdkExtension {

    external fun hookSetStyle(): Unit

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
