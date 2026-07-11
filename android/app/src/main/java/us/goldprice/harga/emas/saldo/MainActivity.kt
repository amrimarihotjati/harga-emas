package us.goldprice.harga.emas.saldo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import us.goldprice.harga.emas.saldo.di.AppContainer
import us.goldprice.harga.emas.saldo.presentation.MainViewModel
import us.goldprice.harga.emas.saldo.presentation.navigation.AppNavigation
import us.goldprice.harga.emas.saldo.theme.HargaEmasHariIniTheme

import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        MobileAds.initialize(this) {}
        
        // Manual DI initialized here
        val appContainer = AppContainer(applicationContext)
        val viewModel = MainViewModel(appContainer.goldRepository)
        
        val simulationViewModel = us.goldprice.harga.emas.saldo.presentation.simulation.SimulationViewModel(
            appContainer.calculateSellUseCase,
            appContainer.calculateBuyUseCase,
            appContainer.calculateBudgetUseCase,
            appContainer.calculateTargetUseCase,
            appContainer.calculatePortfolioUseCase
        )

        setContent {
            HargaEmasHariIniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel, simulationViewModel = simulationViewModel)
                }
            }
        }
    }
}
