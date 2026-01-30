package com.example.appmobile

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://mcmfejxtqusxpjwjgean.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1jbWZlanh0cXVzeHBqd2pnZWFuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Njc5MDYwMDAsImV4cCI6MjA4MzQ4MjAwMH0.jKfY57zIhbvqsUu9sEcxeNjnlFoKtk2kRHV0HDNbpYA"
    ) {
        install(Auth)
        install(Postgrest)
    }
}