package com.simplifier.circlebarnfc.presentation.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CoroutineHelper {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    fun runOnIOThread(block: suspend CoroutineScope.() -> Unit) {
        ioScope.launch {
            block()
        }
    }

    fun runOnMainThread(block: suspend CoroutineScope.() -> Unit) {
        mainScope.launch {
            block()
        }
    }

    fun runOnDefaultThread(block: suspend CoroutineScope.() -> Unit) {
        defaultScope.launch {
            block()
        }
    }
}