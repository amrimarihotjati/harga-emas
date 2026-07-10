package us.goldprice.hargaemas.di

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import us.goldprice.hargaemas.data.GoldApiService
import us.goldprice.hargaemas.data.GoldRepository

class AppContainer {
    private val BASE_URL = "https://raw.githubusercontent.com/amrimarihotjati/harga-emas/main/json/"
    
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
}
