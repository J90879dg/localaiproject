package com.localaiproject.windows.core.client

import com.localaiproject.windows.core.model.LinkedDeviceState

class LocalBridgeClient {
    fun handshake(androidDeviceId: String): LinkedDeviceState {
        val connected = androidDeviceId.isNotBlank()
        return LinkedDeviceState(
            androidConnected = connected,
            lastHandshakeUnix = System.currentTimeMillis() / 1000
        )
    }
}
