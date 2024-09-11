#include <jni.h>
#include <string>
#include <sstream>
#include <android/log.h>
#include "shadowhook.h"

#define LOG_TAG "NativeExt"
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#include <TomTom/NavKit/Map/Map.hpp>
#include <TomTom/NavKit/Map/Environment.hpp>
#include "MapStyler.hpp"
#include <Michi/Style/StyleParser.hpp>
#include <Michi/Style/Style.hpp>

void logLongString(const std::string& str, size_t chunkSize = 1000) {
    size_t length = str.length();
    for (size_t i = 0; i < length; i += chunkSize) {
        std::string chunk = str.substr(i, chunkSize);
        LOG("%s", chunk.c_str());
    }
}

std::string replaceSubstring(const std::string& original, const std::string& from, const std::string& to) {
    std::string result = original;
    size_t pos = 0;

    while ((pos = result.find(from, pos)) != std::string::npos) {
        result.replace(pos, from.length(), to);
        pos += to.length();
    }

    return result;
}

char *locationMarkerColor = nullptr;
double locationMarkerAmbient = 0;
double locationMarkerGamma = 0;

namespace Michi
{
namespace Style
{
class CustomStyleParser : public StyleParserInterface, private boost::noncopyable
{
public:
    StyleSharedPtr parseFromJson(
            const std::string& configString,
            const Common::Filter::PropertyExtractorInterface& constantsSelector) const override {
        LOG("using custom style parser");
        std::string modifiedConfigString = configString;
        std::string from = "\"@position-marker-normal-base-color\": \"#3F9CD9\"";
        std::string to = (std::ostringstream() << "\"@position-marker-normal-base-color\": \"" << locationMarkerColor << "\"").str();
        modifiedConfigString = replaceSubstring(modifiedConfigString, from, to);
        from = "\"@position-marker-nofix-base-color\": \"#CCCCCC\"";
        to = (std::ostringstream() << "\"@position-marker-nofix-base-color\": \"" << locationMarkerColor << "\"").str();
        modifiedConfigString = replaceSubstring(modifiedConfigString, from, to);
        from = "\"ambient\": 0.4,";
        to = (std::ostringstream() << "\"ambient\": " << locationMarkerAmbient << ", \"gamma\": " << locationMarkerGamma << ",").str();
        modifiedConfigString = replaceSubstring(modifiedConfigString, from, to);
        StyleSharedPtr style = StyleParser().parseFromJson(modifiedConfigString, constantsSelector);
        return style;
    }
};
} // namespace Style
} // namespace Michi

typedef jlong (*setStyle11_t)(JNIEnv *, jclass, jlong, jobject, jlong, jobject);
setStyle11_t orig_setStyle11 = nullptr;
jlong hooked_setStyle11(JNIEnv *jenv, jclass jcls, jlong jarg1, jobject jarg1_, jlong jarg2, jobject jarg2_) {
    LOG("in hooked_setStyle11");
    std::shared_ptr< TomTom::NavKit::Map::Map > *smartarg1 = *(std::shared_ptr<  TomTom::NavKit::Map::Map > **)&jarg1;
    TomTom::NavKit::Map::Map *arg1 = (TomTom::NavKit::Map::Map *)(smartarg1 ? smartarg1->get() : nullptr);

    char* basePtr = reinterpret_cast<char*>(arg1);
    TomTom::NavKit::Map::Internal::MapStyler* mapStyler = reinterpret_cast<TomTom::NavKit::Map::Internal::MapStyler*>(basePtr + 480);
    bool hasStyle = mapStyler->hasStyleSet();
    if (hasStyle) {
        LOG("black magic start");
        TomTom::NavKit::Map::Environment* environment = *reinterpret_cast<TomTom::NavKit::Map::Environment**>(basePtr + 424);
        unsigned int dpi = environment->getDpi();
        LOG("dpi = %d", dpi);
        Michi::Style::CustomStyleParser customStyleParser;
        new (mapStyler) TomTom::NavKit::Map::Internal::MapStyler(customStyleParser, dpi);
        LOG("black magic done");
    }
    return orig_setStyle11(jenv, jcls, jarg1, jarg1_, jarg2, jarg2_);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tomtom_sdk_extension_library_NavSdkExtension_customizeLocationMarker(JNIEnv *env,
                                                                              jobject thiz,
                                                                              jstring color,
                                                                              jdouble ambient,
                                                                              jdouble gamma) {
    LOG("hook");
    const char *nativeString = env->GetStringUTFChars(color, nullptr);
    if (locationMarkerColor != nullptr) {
        free(locationMarkerColor);
    }
    locationMarkerColor = (char *)malloc(strlen(nativeString) + 1);
    strcpy(locationMarkerColor, nativeString);

    locationMarkerAmbient = ambient;
    locationMarkerGamma = gamma;

    shadowhook_hook_sym_name(
            "libtomtom-navsdk.so",
            "Java_com_tomtom_sdk_maps_display_engine_TomTomNavKitMapJNI_Map_1setStyle_1_1SWIG_11",
            (void *)hooked_setStyle11,
            (void **)&orig_setStyle11);
}
