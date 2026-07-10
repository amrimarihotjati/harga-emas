package us.goldprice.hargaemas.data

import us.goldprice.hargaemas.domain.GoldData

class GoldRepository(private val api: GoldApiService) {
    suspend fun fetchGoldPrices(): GoldData {
        return api.getGoldPrices()
    }

    suspend fun fetchAdConfig() = api.getAdConfig()
}
