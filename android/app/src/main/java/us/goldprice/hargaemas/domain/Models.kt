package us.goldprice.hargaemas.domain

import com.google.gson.annotations.SerializedName

data class PriceInfo(
    @SerializedName("weight") val weight: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("buy_price") val buyPrice: Long,
    @SerializedName("sell_price") val sellPrice: Long
)

data class GoldData(
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("prices") val prices: List<PriceInfo>
)
