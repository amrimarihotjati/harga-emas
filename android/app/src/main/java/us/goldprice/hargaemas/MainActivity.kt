package us.goldprice.hargaemas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import us.goldprice.hargaemas.di.AppContainer
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.navigation.AppNavigation
import us.goldprice.hargaemas.theme.HargaEmasHariIniTheme

import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        MobileAds.initialize(this) {}
        
        // Manual DI initialized here
        val appContainer = AppContainer()
        val viewModel = MainViewModel(appContainer.goldRepository)

        setContent {
            HargaEmasHariIniTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
