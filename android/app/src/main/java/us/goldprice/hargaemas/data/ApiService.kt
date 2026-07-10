package us.goldprice.hargaemas.data

import retrofit2.http.GET
import us.goldprice.hargaemas.domain.GoldData

interface GoldApiService {
    @GET("prices.json")
    suspend fun getGoldPrices(): GoldData
}
