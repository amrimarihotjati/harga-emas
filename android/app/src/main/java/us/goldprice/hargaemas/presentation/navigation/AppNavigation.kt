@file:Suppress("DEPRECATION")
package us.goldprice.hargaemas.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import us.goldprice.hargaemas.presentation.MainViewModel
import us.goldprice.hargaemas.presentation.home.HomeScreen
import us.goldprice.hargaemas.presentation.compare.CompareScreen
import us.goldprice.hargaemas.presentation.simulation.SimulationScreen
import us.goldprice.hargaemas.theme.Outline
import us.goldprice.hargaemas.theme.Primary
import us.goldprice.hargaemas.theme.Surface
import us.goldprice.hargaemas.theme.SurfaceContainerHighest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Compare : Screen("compare", "Compare", Icons.Default.CompareArrows)
    object Simulation : Screen("simulation", "Simulate", Icons.Default.Calculate)
    object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.AccountBalanceWallet)
}

@Composable
fun AppNavigation(viewModel: MainViewModel, simulationViewModel: us.goldprice.hargaemas.presentation.simulation.SimulationViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Compare,
        Screen.Simulation,
        Screen.Portfolio
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
            ) {
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
                            indicatorColor = SurfaceContainerHighest,
                            unselectedIconColor = Outline,
                            unselectedTextColor = Outline
                        ),
                        onClick = {
                            if (screen.route == Screen.Portfolio.route) {
                                // For now route portfolio to simulation since it has the portfolio tab
                                navController.navigate(Screen.Simulation.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSimulation = {
                        navController.navigate(Screen.Simulation.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Compare.route) {
                CompareScreen(viewModel)
            }
            composable(Screen.Simulation.route) {
                SimulationScreen(viewModel, simulationViewModel)
            }
        }
    }
}
