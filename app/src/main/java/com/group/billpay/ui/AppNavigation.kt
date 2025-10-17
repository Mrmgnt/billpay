package com.group.billpay.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.group.billpay.ui.screens.BillDetailScreen
import com.group.billpay.ui.screens.BillSplitterScreen
import com.group.billpay.ui.viewmodel.BillSplitterViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Buat satu instance ViewModel untuk dibagikan ke semua screen
    val viewModel: BillSplitterViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            BillSplitterScreen(
                viewModel = viewModel,
                onNavigateToBill = { billId ->
                    navController.navigate("billDetail/$billId")
                }
            )
        }
        composable(
            route = "billDetail/{billId}",
            arguments = listOf(navArgument("billId") { type = NavType.LongType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getLong("billId") ?: -1
            BillDetailScreen(
                billId = billId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}