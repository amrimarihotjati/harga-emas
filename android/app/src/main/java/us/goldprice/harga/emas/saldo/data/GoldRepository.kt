package us.goldprice.harga.emas.saldo.data

import us.goldprice.harga.emas.saldo.domain.GoldData

import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GoldRepository(
    private val api: GoldApiService,
    private val sharedPrefs: SharedPreferences
) {
    private val gson = Gson()
    private val CACHE_KEY = "gold_data_cache"
    private val AD_CACHE_KEY = "ad_config_cache"

    fun fetchGoldPricesFlow(): Flow<Result<GoldData>> = flow {
        // 1. Emit cached data first if available
        val cachedJson = sharedPrefs.getString(CACHE_KEY, null)
        if (cachedJson != null) {
            try {
                val cachedData = gson.fromJson(cachedJson, GoldData::class.java)
                emit(Result.success(cachedData))
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }

        // 2. Fetch from network
        try {
            val networkData = api.getGoldPrices()
            // Save to cache
            sharedPrefs.edit().putString(CACHE_KEY, gson.toJson(networkData)).apply()
            // Emit fresh data
            emit(Result.success(networkData))
        } catch (e: Exception) {
            // Only emit failure if we don't have cache
            if (cachedJson == null) {
                emit(Result.failure(e))
            }
        }
    }

    suspend fun fetchGoldPrices(): GoldData {
        return api.getGoldPrices()
    }

    suspend fun fetchAdConfig(): AdConfig {
        val cachedJson = sharedPrefs.getString(AD_CACHE_KEY, null)
        return try {
            val networkData = api.getAdConfig()
            sharedPrefs.edit().putString(AD_CACHE_KEY, gson.toJson(networkData)).apply()
            networkData
        } catch (e: Exception) {
            if (cachedJson != null) {
                gson.fromJson(cachedJson, AdConfig::class.java)
            } else {
                throw e
            }
        }
    }

    private val HISTORY_CACHE_KEY = "history_data_cache"

    suspend fun fetchHistory(): List<us.goldprice.harga.emas.saldo.domain.HistoryItem> {
        val cachedJson = sharedPrefs.getString(HISTORY_CACHE_KEY, null)
        return try {
            val networkData = api.getHistory()
            sharedPrefs.edit().putString(HISTORY_CACHE_KEY, gson.toJson(networkData)).apply()
            networkData
        } catch (e: Exception) {
            if (cachedJson != null) {
                gson.fromJson(cachedJson, Array<us.goldprice.harga.emas.saldo.domain.HistoryItem>::class.java).toList()
            } else {
                emptyList()
            }
        }
    }
}
