package com.simats.focusflow.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import com.simats.focusflow.data.repository.ProductivityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NetworkSyncManager private constructor(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var isRegistered = false

    companion object {
        @Volatile
        private var instance: NetworkSyncManager? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = NetworkSyncManager(context.applicationContext).apply {
                            register()
                        }
                    }
                }
            }
        }

        fun getInstance(): NetworkSyncManager? = instance
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            ) {
                triggerSync()
            }
        }

        override fun onAvailable(network: Network) {
            triggerSync()
        }
    }

    fun register() {
        if (isRegistered || connectivityManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(request, networkCallback)
            }
            isRegistered = true
        } catch (_: Exception) {}
    }

    private val isSyncing = java.util.concurrent.atomic.AtomicBoolean(false)

    fun triggerSync() {
        scope.launch {
            if (isSyncing.compareAndSet(false, true)) {
                try {
                    ProductivityRepository.syncPendingItems()
                } catch (_: Exception) {
                } finally {
                    isSyncing.set(false)
                }
            }
        }
    }
}
