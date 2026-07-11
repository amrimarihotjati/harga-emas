package us.goldprice.harga.emas.saldo.domain.usecase

import us.goldprice.harga.emas.saldo.domain.PriceInfo

data class SellSimulationResult(
    val buyPriceInput: Long,
    val sellPriceToday: Long,
    val capitalValue: Long,
    val sellValue: Long,
    val profitLoss: Long,
    val profitPercentage: Double,
    val status: String // "Untung", "Rugi", "Impas"
)

class CalculateSellUseCase {
    operator fun invoke(
        gram: Double,
        buyPricePerGram: Long,
        todayPriceInfo: PriceInfo?
    ): SellSimulationResult {
        val todaySellPrice = todayPriceInfo?.buyPrice ?: 0L // Remember, when user sells to vendor, vendor "buys" it (buyPrice of vendor)
        
        val capitalValue = (buyPricePerGram * gram).toLong()
        val sellValue = (todaySellPrice * gram).toLong()
        
        val profitLoss = sellValue - capitalValue
        val profitPercentage = if (capitalValue > 0) (profitLoss.toDouble() / capitalValue) * 100 else 0.0
        
        val status = when {
            profitLoss > 0 -> "Untung"
            profitLoss < 0 -> "Rugi"
            else -> "Impas"
        }
        
        return SellSimulationResult(
            buyPriceInput = buyPricePerGram,
            sellPriceToday = todaySellPrice,
            capitalValue = capitalValue,
            sellValue = sellValue,
            profitLoss = profitLoss,
            profitPercentage = profitPercentage,
            status = status
        )
    }
}
