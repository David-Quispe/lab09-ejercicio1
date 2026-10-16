package com.example.lab09_ejercicio1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab09_ejercicio1.ui.CatalogScreen
import com.example.lab09_ejercicio1.ui.PokedexViewModel
import com.example.lab09_ejercicio1.ui.PokemonDetailScreen
import com.example.lab09_ejercicio1.ui.theme.Lab09ejercicio1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab09ejercicio1Theme {
                PokedexApp()
            }
        }
    }
}

@Composable
private fun PokedexApp(viewModel: PokedexViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "catalogo") {
        composable("catalogo") {
            CatalogScreen(
                state = state,
                onLoad = viewModel::loadCatalog,
                onQueryChange = viewModel::updateQuery,
                onShowMore = viewModel::showMore,
                onPokemonClick = { id -> navController.navigate("pokemon/$id") }
            )
        }
        composable(
            "pokemon/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            PokemonDetailScreen(
                id = id,
                state = state,
                onLoad = viewModel::loadProfile,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
