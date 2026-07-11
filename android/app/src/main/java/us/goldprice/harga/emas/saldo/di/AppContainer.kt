package us.goldprice.harga.emas.saldo.di

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.goldprice.harga.emas.saldo.data.GoldApiService
import us.goldprice.harga.emas.saldo.data.GoldRepository
import us.goldprice.harga.emas.saldo.domain.usecase.*

import android.content.Context
import android.content.SharedPreferences

class AppContainer(private val context: Context) {
    private val BASE_URL = "https://code.amrimarihotjati.workers.dev/harga-emas/"
    
    val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("harga_emas_prefs", Context.MODE_PRIVATE)
    }
    
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: GoldApiService by lazy {
        retrofit.create(GoldApiService::class.java)
    }

    val goldRepository: GoldRepository by lazy {
        GoldRepository(retrofitService, sharedPreferences)
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
