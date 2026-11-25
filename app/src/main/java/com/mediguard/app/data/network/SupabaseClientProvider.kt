package com.mediguard.app.data.network

import com.mediguard.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {

    // URL dan Anon Public Key diambil dari BuildConfig (local.properties)
    val DEFAULT_SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    val DEFAULT_SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY

    var supabaseUrl: String = DEFAULT_SUPABASE_URL
        private set

    var supabaseAnonKey: String = DEFAULT_SUPABASE_ANON_KEY
        private set

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("YOUR_PROJECT_ID") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("YOUR_ANON_PUBLIC_KEY")

    @Volatile
    private var _client: SupabaseClient? = null

    val client: SupabaseClient
        get() {
            return _client ?: synchronized(this) {
                _client ?: createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseAnonKey
                ) {
                    install(Postgrest)
                    install(Realtime)
                    install(Storage)
                }.also { _client = it }
            }
        }

    fun configure(url: String, key: String) {
        val cleanUrl = url.trim()
        val cleanKey = key.trim()
        if (cleanUrl != supabaseUrl || cleanKey != supabaseAnonKey) {
            supabaseUrl = cleanUrl
            supabaseAnonKey = cleanKey
            _client = null // Invalidate existing client so new config is applied on next access
        }
    }
}
