package org.example.callback

interface InstallDataCallback {
    fun onSuccess(msg: String)
    fun onError(e: String)
}