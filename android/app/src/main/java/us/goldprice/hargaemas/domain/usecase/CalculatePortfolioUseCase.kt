package us.goldprice.hargaemas.domain.usecase

import us.goldprice.hargaemas.domain.PriceInfo

data class PortfolioAsset(
    val vendorUnit: String,
    val gram: Double,
    val buyPricePerGram: Long
)

data class PortfolioAssetResult(
    val asset: PortfolioAsset,
    val currentSellPricePerGram: Long,
    val capitalValue: Long,
    val currentValue: Long,
    val profitLoss: Long
)

data class PortfolioSimulationResult(
    val totalGram: Double,
    val totalCapital: Long,
    val totalCurrentValue: Long,
    val totalProfitLoss: Long,
    val totalProfitPercentage: Double,
    val assetResults: List<PortfolioAssetResult>
)

class CalculatePortfolioUseCase {
    operator fun invoke(
        assets: List<PortfolioAsset>,
        todayPrices: List<PriceInfo>
    ): PortfolioSimulationResult {
        var totalGram = 0.0
        var totalCapital = 0L
        var totalCurrentValue = 0L
        
        val assetResults = assets.map { asset ->
            // Match vendor by unit
            val currentPriceInfo = todayPrices.find { it.unit == asset.vendorUnit && it.weight == "1" } 
                ?: todayPrices.find { it.unit == asset.vendorUnit }
                
            val currentSellPrice = currentPriceInfo?.buyPrice ?: 0L // User sells, vendor buys
            
            val capital = (asset.gram * asset.buyPricePerGram).toLong()
            val currentVal = (asset.gram * currentSellPrice).toLong()
            val profitLoss = currentVal - capital
            
            totalGram += asset.gram
            totalCapital += capital
            totalCurrentValue += currentVal
            
            PortfolioAssetResult(
                asset = asset,
                currentSellPricePerGram = currentSellPrice,
                capitalValue = capital,
                currentValue = currentVal,
                profitLoss = profitLoss
            )
        }
        
        val totalProfitLoss = totalCurrentValue - totalCapital
        val totalProfitPercentage = if (totalCapital > 0) (totalProfitLoss.toDouble() / totalCapital) * 100 else 0.0
        
        return PortfolioSimulationResult(
            totalGram = totalGram,
            totalCapital = totalCapital,
            totalCurrentValue = totalCurrentValue,
            totalProfitLoss = totalProfitLoss,
            totalProfitPercentage = totalProfitPercentage,
            assetResults = assetResults
        )
    }
}
