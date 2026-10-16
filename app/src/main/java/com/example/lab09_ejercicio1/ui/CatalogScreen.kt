package com.example.lab09_ejercicio1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lab09_ejercicio1.data.NamedResource
import com.example.lab09_ejercicio1.data.artworkUrl
import com.example.lab09_ejercicio1.data.displayName
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: PokedexUiState,
    onLoad: () -> Unit,
    onQueryChange: (String) -> Unit,
    onShowMore: () -> Unit,
    onPokemonClick: (Int) -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    val query = state.query.trim().lowercase(Locale.ROOT)
    val filtered = if (query.isBlank()) {
        state.catalog
    } else {
        state.catalog.filter { entry ->
            entry.name.contains(query) || entry.id.toString().startsWith(query)
        }
    }
    val visible = filtered.take(state.visibleCount)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PokeballMark(size = 26.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Pokédex", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Descubre cada Pokémon",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Busca por nombre o número y explora su ficha.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            PokeballMark(
                                size = 70.dp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f)
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre o número") },
                        label = { Text("Buscar Pokémon") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (state.query.isNotEmpty()) {
                            {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (query.isBlank()) "Explora la Pokédex" else "Resultados de búsqueda",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${filtered.size} Pokémon disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            when {
                state.catalogLoading -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CatalogMessage("Cargando Pokémon…", true)
                }
                state.catalogError != null -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CatalogMessage(state.catalogError, false)
                        Button(onClick = onLoad) { Text("Reintentar") }
                    }
                }
                filtered.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CatalogMessage("No encontramos Pokémon con ese nombre o número.", false)
                        OutlinedButton(onClick = { onQueryChange("") }) {
                            Text("Limpiar búsqueda")
                        }
                    }
                }
                else -> {
                    items(visible, key = { it.name }) { pokemon ->
                        PokemonTile(pokemon, onClick = { onPokemonClick(pokemon.id) })
                    }
                    if (filtered.size > visible.size) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                                OutlinedButton(onClick = onShowMore) { Text("Mostrar más Pokémon") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonTile(pokemon: NamedResource, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "N.º ${pokemon.id.toString().padStart(4, '0')}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier.fillMaxWidth().height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(110.dp).background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
                )
                AsyncImage(
                    model = artworkUrl(pokemon.id),
                    contentDescription = "Ilustración de ${displayName(pokemon.name)}",
                    modifier = Modifier.size(122.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                displayName(pokemon.name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Ver ficha",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CatalogMessage(message: String, loading: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 45.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(18.dp))
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
