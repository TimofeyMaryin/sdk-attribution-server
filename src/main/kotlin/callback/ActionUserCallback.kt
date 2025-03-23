package org.example.callback

import org.example.model.UserModel

interface ActionUserCallback {
    fun onSuccess(msg: String, data: UserModel)
    fun onError(e: String)
}