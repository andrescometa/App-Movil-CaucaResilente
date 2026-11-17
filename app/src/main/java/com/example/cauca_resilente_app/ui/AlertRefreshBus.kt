// ui/AlertRefreshBus.kt
package com.example.cauca_resilente_app.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object AlertRefreshBus {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events

    fun triggerRefresh() {
        _events.tryEmit(Unit)
    }
}