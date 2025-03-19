package org.example.callback

interface ActionDataCallback {
    fun onSuccess(msg: String)
    fun onError(e: String)
}
