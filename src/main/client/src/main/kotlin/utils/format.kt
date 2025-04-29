package org.example.utils

import org.example.model.InstallData

fun <K> Map<K, List<InstallData>>.format(): String {
    return this.entries.joinToString(separator = "; ") { (key, data) ->
        "${key.toString()} - ${data.size}"
    }
}