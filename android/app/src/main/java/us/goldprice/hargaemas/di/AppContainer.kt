package us.goldprice.hargaemas.di

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.goldprice.hargaemas.data.GoldApiService
import us.goldprice.hargaemas.data.GoldRepository
import us.goldprice.hargaemas.domain.usecase.*

class AppContainer {
    private val BASE_URL = "https://code.amrimarihotjati.workers.dev/harga-emas/"
    
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: GoldApiService by lazy {
        retrofit.create(GoldApiService::class.java)
    }

    val goldRepository: GoldRepository by lazy {
        GoldRepository(retrofitService)
    }

    val calculateSellUseCase: CalculateSellUseCase by lazy {
        CalculateSellUseCase()
    }

    val calculateBuyUseCase: CalculateBuyUseCase by lazy {
        CalculateBuyUseCase()
    }

    val calculateBudgetUseCase: CalculateBudgetUseCase by lazy {
        CalculateBudgetUseCase()
    }

    val calculateTargetUseCase: CalculateTargetUseCase by lazy {
        CalculateTargetUseCase()
    }

    val calculatePortfolioUseCase: CalculatePortfolioUseCase by lazy {
        CalculatePortfolioUseCase()
    }
}
