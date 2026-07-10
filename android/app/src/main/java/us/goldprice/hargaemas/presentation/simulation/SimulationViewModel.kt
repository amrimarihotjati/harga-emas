package us.goldprice.hargaemas.presentation.simulation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import us.goldprice.hargaemas.domain.PriceInfo
import us.goldprice.hargaemas.domain.usecase.*

class SimulationViewModel(
    private val calculateSellUseCase: CalculateSellUseCase,
    private val calculateBuyUseCase: CalculateBuyUseCase,
    private val calculateBudgetUseCase: CalculateBudgetUseCase,
    private val calculateTargetUseCase: CalculateTargetUseCase,
    private val calculatePortfolioUseCase: CalculatePortfolioUseCase
) : ViewModel() {

    // Tab 1: Sell
    private val _sellResult = MutableStateFlow<SellSimulationResult?>(null)
    val sellResult: StateFlow<SellSimulationResult?> = _sellResult.asStateFlow()

    fun calculateSell(gramStr: String, buyPriceStr: String, vendorUnit: String, todayPrices: List<PriceInfo>) {
        val gram = gramStr.toDoubleOrNull() ?: return
        val buyPrice = buyPriceStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: return
        
        val priceInfo = todayPrices.find { it.unit == vendorUnit && it.weight == "1" } 
            ?: todayPrices.find { it.unit == vendorUnit }
            
        val result = calculateSellUseCase(gram, buyPrice, priceInfo)
        _sellResult.update { result }
    }

    fun clearSell() { _sellResult.update { null } }

    // Tab 2: Buy
    private val _buyResult = MutableStateFlow<BuySimulationResult?>(null)
    val buyResult: StateFlow<BuySimulationResult?> = _buyResult.asStateFlow()

    fun calculateBuy(gramStr: String, vendorUnit: String, todayPrices: List<PriceInfo>) {
        val gram = gramStr.toDoubleOrNull() ?: return
        val priceInfo = todayPrices.find { it.unit == vendorUnit && it.weight == "1" } 
            ?: todayPrices.find { it.unit == vendorUnit }
            
        val result = calculateBuyUseCase(gram, priceInfo)
        _buyResult.update { result }
    }

    fun clearBuy() { _buyResult.update { null } }

    // Tab 3: Budget
    private val _budgetResult = MutableStateFlow<BudgetSimulationResult?>(null)
    val budgetResult: StateFlow<BudgetSimulationResult?> = _budgetResult.asStateFlow()

    fun calculateBudget(budgetStr: String, vendorUnit: String, todayPrices: List<PriceInfo>) {
        val budget = budgetStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: return
        val priceInfo = todayPrices.find { it.unit == vendorUnit && it.weight == "1" } 
            ?: todayPrices.find { it.unit == vendorUnit }
            
        val result = calculateBudgetUseCase(budget, priceInfo)
        _budgetResult.update { result }
    }

    fun clearBudget() { _budgetResult.update { null } }

    // Tab 4: Target
    private val _targetResult = MutableStateFlow<TargetSimulationResult?>(null)
    val targetResult: StateFlow<TargetSimulationResult?> = _targetResult.asStateFlow()

    fun calculateTarget(targetStr: String, vendorUnit: String, todayPrices: List<PriceInfo>) {
        val targetGram = targetStr.toDoubleOrNull() ?: return
        val priceInfo = todayPrices.find { it.unit == vendorUnit && it.weight == "1" } 
            ?: todayPrices.find { it.unit == vendorUnit }
            
        val result = calculateTargetUseCase(targetGram, priceInfo)
        _targetResult.update { result }
    }

    fun clearTarget() { _targetResult.update { null } }

    // Tab 5: Portfolio
    private val _portfolioAssets = MutableStateFlow<List<PortfolioAsset>>(emptyList())
    val portfolioAssets: StateFlow<List<PortfolioAsset>> = _portfolioAssets.asStateFlow()
    
    private val _portfolioResult = MutableStateFlow<PortfolioSimulationResult?>(null)
    val portfolioResult: StateFlow<PortfolioSimulationResult?> = _portfolioResult.asStateFlow()

    fun addPortfolioAsset(vendorUnit: String, gramStr: String, buyPriceStr: String, todayPrices: List<PriceInfo>) {
        val gram = gramStr.toDoubleOrNull() ?: return
        val buyPrice = buyPriceStr.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: return
        
        val newAsset = PortfolioAsset(vendorUnit, gram, buyPrice)
        _portfolioAssets.update { it + newAsset }
        
        recalculatePortfolio(todayPrices)
    }
    
    fun clearPortfolioAssets(todayPrices: List<PriceInfo>) {
        _portfolioAssets.update { emptyList() }
        recalculatePortfolio(todayPrices)
    }

    fun recalculatePortfolio(todayPrices: List<PriceInfo>) {
        if (_portfolioAssets.value.isEmpty()) {
            _portfolioResult.update { null }
            return
        }
        val result = calculatePortfolioUseCase(_portfolioAssets.value, todayPrices)
        _portfolioResult.update { result }
    }
}
