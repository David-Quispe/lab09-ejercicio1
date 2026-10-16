package com.example.lab09_ejercicio1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab09_ejercicio1.ui.CatalogScreen
import com.example.lab09_ejercicio1.ui.PokedexViewModel
import com.example.lab09_ejercicio1.ui.PokedexUiState
import com.example.lab09_ejercicio1.ui.PokemonDetailScreen
import com.example.lab09_ejercicio1.ui.TeamScreen
import com.example.lab09_ejercicio1.ui.theme.Lab09ejercicio1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PokedexViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()
            val darkTheme = state.darkThemeOverride ?: isSystemInDarkTheme()
            Lab09ejercicio1Theme(darkTheme = darkTheme) {
                PokedexApp(viewModel, state, darkTheme)
            }
        }
    }
}

@Composable
private fun PokedexApp(viewModel: PokedexViewModel, state: PokedexUiState, darkTheme: Boolean) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.notice) {
        state.notice?.let {
            snackbar.showSnackbar(it)
            viewModel.clearNotice()
        }
    }
    val topLevel = listOf("catalogo", "capturados", "equipo")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (route in topLevel) NavigationBar {
                NavigationBarItem(
                    selected = route == "catalogo",
                    onClick = { navController.navigateTopLevel("catalogo") },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Explorar") }
                )
                NavigationBarItem(
                    selected = route == "capturados",
                    onClick = { navController.navigateTopLevel("capturados") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Capturados") }
                )
                NavigationBarItem(
                    selected = route == "equipo",
                    onClick = { navController.navigateTopLevel("equipo") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Equipo") }
                )
            }
        }
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = "catalogo",
            modifier = Modifier.padding(outerPadding)
        ) {
            listOf(false to "catalogo", true to "capturados").forEach { (favoritesOnly, destination) ->
                composable(destination) {
                    CatalogScreen(
                        state = state,
                        reference = viewModel.reference,
                        favoritesOnly = favoritesOnly,
                        darkTheme = darkTheme,
                        onToggleTheme = { viewModel.toggleDarkTheme(darkTheme) },
                        onLoad = viewModel::loadCatalog,
                        onQueryChange = viewModel::updateQuery,
                        onShowMore = viewModel::showMore,
                        onPokemonClick = { id -> navController.navigate("pokemon/$id") },
                        onFavorite = viewModel::toggleFavorite,
                        onTeam = viewModel::toggleTeam,
                        onGeneration = viewModel::setGeneration,
                        onType = viewModel::setType,
                        onEggGroup = viewModel::setEggGroup,
                        onSort = viewModel::setSort,
                        onClearFilters = viewModel::clearFilters
                    )
                }
            }
            composable("equipo") {
                TeamScreen(
                    state = state,
                    reference = viewModel.reference,
                    darkTheme = darkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme(darkTheme) },
                    onExplore = { navController.navigateTopLevel("catalogo") },
                    onPokemonClick = { id -> navController.navigate("pokemon/$id") },
                    onRemove = viewModel::toggleTeam
                )
            }
            composable("pokemon/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                PokemonDetailScreen(
                    id = id,
                    state = state,
                    reference = viewModel.reference,
                    darkTheme = darkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme(darkTheme) },
                    onLoad = viewModel::loadProfile,
                    onBack = { navController.popBackStack() },
                    onFavorite = viewModel::toggleFavorite,
                    onTeam = viewModel::toggleTeam,
                    onEvolutionClick = { target -> navController.navigate("pokemon/$target") }
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo("catalogo") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
