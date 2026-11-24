package com.mediguard.app

import com.mediguard.app.data.network.SupabaseClientProvider
import com.mediguard.app.data.repository.MediGuardRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseIntegrationTest {

    @Test
    fun testSupabaseClientProvider_defaultStateNotConfigured() {
        // Reset to default
        SupabaseClientProvider.configure(
            SupabaseClientProvider.DEFAULT_SUPABASE_URL,
            SupabaseClientProvider.DEFAULT_SUPABASE_ANON_KEY
        )
        assertFalse(SupabaseClientProvider.isConfigured)
    }

    @Test
    fun testSupabaseClientProvider_configurationWithValidCredentials() {
        SupabaseClientProvider.configure(
            "https://testproject123.supabase.co",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_token_12345"
        )
        assertTrue(SupabaseClientProvider.isConfigured)

        val repository = MediGuardRepository()
        assertTrue(repository.isSupabaseConnected)

        // Reset back to default
        SupabaseClientProvider.configure(
            SupabaseClientProvider.DEFAULT_SUPABASE_URL,
            SupabaseClientProvider.DEFAULT_SUPABASE_ANON_KEY
        )
        assertFalse(repository.isSupabaseConnected)
    }
}
