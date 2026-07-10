package us.goldprice.hargaemas.data

import retrofit2.http.GET
import us.goldprice.hargaemas.domain.GoldData

data class AdConfig(
    val interstitial_id: String,
    val native_id: String,
    val interstitial_click_interval: Int,
    val show_native_on_home: Boolean,
    val show_native_on_compare: Boolean,
    val show_native_on_simulation: Boolean?,
    val show_native_on_portfolio: Boolean?
)

interface GoldApiService {
    @GET("prices.json")
    suspend fun getGoldPrices(): GoldData
    
    @GET("ad_config.json")
    suspend fun getAdConfig(): AdConfig
}
