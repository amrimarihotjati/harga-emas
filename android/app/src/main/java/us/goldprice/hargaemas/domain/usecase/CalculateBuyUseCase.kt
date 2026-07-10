package us.goldprice.hargaemas.domain.usecase

import us.goldprice.hargaemas.domain.PriceInfo

data class BuySimulationResult(
    val pricePerGram: Long,
    val subtotal: Long,
    val tax: Long = 0, // Placeholder for future tax logic
    val adminFee: Long = 0, // Placeholder for future admin fee logic
    val grandTotal: Long
)

class CalculateBuyUseCase {
    operator fun invoke(
        gram: Double,
        todayPriceInfo: PriceInfo?
    ): BuySimulationResult {
        // When user buys, they pay the vendor's "sellPrice"
        val pricePerGram = todayPriceInfo?.sellPrice ?: 0L
        
        val subtotal = (pricePerGram * gram).toLong()
        val tax = 0L // Can be updated if Galeri24/Antam tax applies
        val adminFee = 0L // Can be updated if needed
        val grandTotal = subtotal + tax + adminFee
        
        return BuySimulationResult(
            pricePerGram = pricePerGram,
            subtotal = subtotal,
            tax = tax,
            adminFee = adminFee,
            grandTotal = grandTotal
        )
    }
}
