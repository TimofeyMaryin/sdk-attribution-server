package org.example.utils

enum class FILTER(val query: String) {
    NAME("name"),
    BUNDLE_ID("bundleID"),
    FROM_DATA("fromData"),
    TO_DATA("toData"),
    FROM_API_LEVEL("fromApiLevel"),
    TO_API_LEVEL("toApiLevel"),
    FROM_ANDROID_API_LEVEL("fromAndroidApiLevel"),
    TO_ANDROID_API_LEVEL("toAndroidApiLevel"),
    COUNTRY("country"),
}