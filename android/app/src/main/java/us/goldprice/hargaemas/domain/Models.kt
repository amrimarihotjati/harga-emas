package us.goldprice.hargaemas.domain

import com.google.gson.annotations.SerializedName

data class PriceInfo(
    @SerializedName("weight") val weight: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("buy_price") val buyPrice: Long,
    @SerializedName("sell_price") val sellPrice: Long,
    @SerializedName("change_nominal") val changeNominal: Long = 0,
    @SerializedName("change_percentage") val changePercentage: Double = 0.0,
    @SerializedName("trend") val trend: String? = "flat"
)

data class GoldData(
    @SerializedName("last_updated") val lastUpdated: String,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("prices") val prices: List<PriceInfo>
)
