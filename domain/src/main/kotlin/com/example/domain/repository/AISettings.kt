package com.voltai.domain.repository

interface AISettings {
    suspend fun isAIEnabled(): Boolean
    suspend fun getAIProvider(): String
    suspend fun getAPIKey(provider: String): String?
    suspend fun getCustomAPIUrl(): String?
}