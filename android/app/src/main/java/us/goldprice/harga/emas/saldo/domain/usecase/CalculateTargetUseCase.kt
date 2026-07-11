package us.goldprice.harga.emas.saldo.domain.usecase

import us.goldprice.harga.emas.saldo.domain.PriceInfo

data class TargetSimulationResult(
    val pricePerGram: Long,
    val totalCost: Long,
    val tax: Long = 0,
    val grandTotal: Long
)

class CalculateTargetUseCase {
    operator fun invoke(
        targetGram: Double,
        todayPriceInfo: PriceInfo?
    ): TargetSimulationResult {
        val pricePerGram = todayPriceInfo?.sellPrice ?: 0L
        
        val totalCost = (pricePerGram * targetGram).toLong()
        val tax = 0L // Placeholder
        
        return TargetSimulationResult(
            pricePerGram = pricePerGram,
            totalCost = totalCost,
            tax = tax,
            grandTotal = totalCost + tax
        )
    }
}
