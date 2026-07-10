@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.compare.CompareScreen
import us.goldprice.hargaemas.presentation.home.HomeScreen
import us.goldprice.hargaemas.presentation.portfolio.PortfolioScreen
import us.goldprice.hargaemas.presentation.simulation.SimulationScreen
import us.goldprice.hargaemas.theme.*
import us.goldprice.hargaemas.ads.AdManager
import us.goldprice.hargaemas.presentation.MainUiState
import androidx.compose.runtime.collectAsState
import android.app.Activity

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Beranda", Icons.Default.Home)
    object Compare : Screen("compare", "Bandingkan", Icons.Default.CompareArrows)
    object Simulation : Screen("simulation", "Simulasi", Icons.Default.Calculate)
    object Portfolio : Screen("portfolio", "Portofolio", Icons.Default.PieChart)
}

@Composable
fun AppNavigation(viewModel: MainViewModel, simulationViewModel: us.goldprice.hargaemas.presentation.simulation.SimulationViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Compare, Screen.Simulation, Screen.Portfolio)
    val uiState by viewModel.uiState.collectAsState()
    val adConfig = (uiState as? MainUiState.Success)?.adConfig
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(adConfig) {
        if (adConfig != null) {
            AdManager.loadInterstitial(context, adConfig)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SurfaceContainerLowest) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            indicatorColor = PrimaryFixed,
                            unselectedIconColor = Outline,
                            unselectedTextColor = Outline
                        ),
                        onClick = {
                            if (activity != null) {
                                AdManager.showInterstitialIfReady(activity, adConfig)
                            }
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    simulationViewModel = simulationViewModel,
                    onNavigateToSimulation = {
                        navController.navigate(Screen.Simulation.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToPortfolio = {
                        navController.navigate(Screen.Portfolio.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Compare.route) { CompareScreen(viewModel) }
            composable(Screen.Simulation.route) { SimulationScreen(viewModel, simulationViewModel) }
            composable(Screen.Portfolio.route) { PortfolioScreen(viewModel, simulationViewModel) }
        }
    }
}
