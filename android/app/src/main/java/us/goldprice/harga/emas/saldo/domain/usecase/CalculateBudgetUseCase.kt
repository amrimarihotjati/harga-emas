package us.goldprice.harga.emas.saldo.domain.usecase

import us.goldprice.harga.emas.saldo.domain.PriceInfo

data class BudgetSimulationResult(
    val pricePerGram: Long,
    val estimatedGrams: Double,
    val remainingBudget: Long
)

class CalculateBudgetUseCase {
    operator fun invoke(
        budget: Long,
        todayPriceInfo: PriceInfo?
    ): BudgetSimulationResult {
        val pricePerGram = todayPriceInfo?.sellPrice ?: 0L
        
        if (pricePerGram <= 0) {
            return BudgetSimulationResult(0, 0.0, budget)
        }
        
        val estimatedGrams = budget.toDouble() / pricePerGram
        
        // Let's assume you can buy fractional grams down to 2 decimal places in some digital apps,
        // or just calculate pure math. For physical gold, you'd find the largest denomination,
        // but the prompt says "Gram yang didapat: 2,54 gram", indicating fractional math.
        
        val roundedGrams = Math.round(estimatedGrams * 100.0) / 100.0
        val cost = (roundedGrams * pricePerGram).toLong()
        val remainingBudget = budget - cost
        
        return BudgetSimulationResult(
            pricePerGram = pricePerGram,
            estimatedGrams = roundedGrams,
            remainingBudget = remainingBudget
        )
    }
}
