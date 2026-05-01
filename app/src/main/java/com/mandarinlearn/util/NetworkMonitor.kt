// NetworkMonitor.kt — Mandarin Learn
// Wraps ConnectivityManager.NetworkCallback to expose network state.
// Per ARCHITECTURE.md §4.5.

package com.mandarinlearn.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * Observes network connectivity and provides both a reactive [Flow<Boolean>] and a
 * synchronous [isOnline] check for pre-call guards.
 *
 * Lifecycle: holds a ConnectivityManager.NetworkCallback while observed.
 * The callback is automatically released when the flow collector cancels.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * True iff the device currently has an internet-capable active network.
     * Uses [NetworkCapabilities.NET_CAPABILITY_INTERNET] which is present even on Wi-Fi
     * before a captive-portal check completes. Suitable for fast pre-call guards.
     */
    fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Reactive network-availability stream.
     * Emits [true] when a validated internet-capable network becomes available,
     * [false] when it is lost. Replays the current state immediately on subscription.
     *
     * [conflate] ensures that a slow collector never queues up more than the latest value.
     */
    val networkState: Flow<Boolean> = callbackFlow {
        // Send current state immediately so collectors start with the right value
        trySend(isOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Re-check: another network may still be available
                trySend(isOnline())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities,
            ) {
                trySend(
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                )
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Unregister when the collector cancels (e.g. ViewModel cleared)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.conflate()
}
