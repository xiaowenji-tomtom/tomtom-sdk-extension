package com.tomtom.sdk.extension.library

import com.bytedance.shadowhook.ShadowHook
import com.bytedance.shadowhook.ShadowHook.ConfigBuilder

class NavSdkExtension {

    /**
     * A native method that is implemented by the 'library' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun hookForSmoothTransition(): Unit

    companion object {
        // Used to load the 'library' library on application startup.
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
