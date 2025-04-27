package com.example.connectme

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Globals {

    const val BASE_URL = "http://10.0.2.2/connectme/"


    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
