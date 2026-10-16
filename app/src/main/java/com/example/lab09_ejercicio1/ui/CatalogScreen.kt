package com.example.lab09_ejercicio1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lab09_ejercicio1.data.NamedResource
import com.example.lab09_ejercicio1.data.PokemonReferenceData
import com.example.lab09_ejercicio1.data.artworkUrl
import com.example.lab09_ejercicio1.data.displayName
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: PokedexUiState,
    reference: PokemonReferenceData,
    favoritesOnly: Boolean,
    onLoad: () -> Unit,
    onQueryChange: (String) -> Unit,
    onShowMore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onFavorite: (Int) -> Unit,
    onTeam: (Int) -> Unit,
    onGeneration: (Int?) -> Unit,
    onType: (String?) -> Unit,
    onEggGroup: (String?) -> Unit,
    onSort: (CatalogSort) -> Unit,
    onClearFilters: () -> Unit
) {
    LaunchedEffect(Unit) { onLoad() }
    val query = state.query.trim().lowercase(Locale.ROOT)
    val filtered = remember(state.catalog, state.query, state.generation, state.type,
        state.eggGroup, state.sort, state.favorites, favoritesOnly) {
        val matches = state.catalog.filter { entry ->
            val metadata = reference.index[entry.id]
            (!favoritesOnly || entry.id in state.favorites) &&
                (query.isBlank() || entry.name.contains(query) || entry.id.toString().startsWith(query)) &&
                (state.generation == null || metadata?.generation == state.generation) &&
                (state.type == null || state.type in (metadata?.types ?: emptyList())) &&
                (state.eggGroup == null || state.eggGroup in (metadata?.eggs ?: emptyList()))
        }
        when (state.sort) {
            CatalogSort.NUMBER -> matches.sortedBy(NamedResource::id)
            CatalogSort.ATTACK -> matches.sortedByDescending { reference.index[it.id]?.attack ?: 0 }
            CatalogSort.WEIGHT -> matches.sortedByDescending { reference.index[it.id]?.weight ?: 0 }
            CatalogSort.HEIGHT -> matches.sortedByDescending { reference.index[it.id]?.height ?: 0 }
            CatalogSort.TOTAL -> matches.sortedByDescending { reference.index[it.id]?.total ?: 0 }
        }
    }
    val visible = filtered.take(state.visibleCount)
    val eggGroups = remember(reference) {
        reference.index.values.flatMap { it.eggs }.distinct().sorted()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PokeballMark(size = 26.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(if (favoritesOnly) "Mis capturados" else "Pokédex", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    if (!favoritesOnly) {
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
                                    Text("Descubre cada Pokémon", style = MaterialTheme.typography.headlineMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Filtra, compara y crea tu equipo.", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.width(8.dp))
                                PokeballMark(size = 70.dp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.45f))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre o número") },
                        label = { Text("Buscar Pokémon") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (state.query.isNotEmpty()) {{
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                            }
                        }} else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectorChip(
                            label = state.generation?.let { generationName(it) } ?: "Generación",
                            selected = state.generation != null,
                            options = listOf("Todas" to null) + (1..9).map { generationName(it) to it },
                            onSelect = onGeneration
                        )
                        SelectorChip(
                            label = state.type?.let(::typeName) ?: "Tipo",
                            selected = state.type != null,
                            options = listOf("Todos" to null) + reference.types.map { typeName(it) to it },
                            onSelect = onType
                        )
                        SelectorChip(
                            label = state.eggGroup?.let(::eggGroupName) ?: "Grupo huevo",
                            selected = state.eggGroup != null,
                            options = listOf("Todos" to null) + eggGroups.map { eggGroupName(it) to it },
                            onSelect = onEggGroup
                        )
                        SelectorChip(
                            label = state.sort.label,
                            selected = state.sort != CatalogSort.NUMBER,
                            options = CatalogSort.entries.map { it.label to it },
                            onSelect = onSort
                        )
                    }
                    if (state.generation != null || state.type != null || state.eggGroup != null || state.sort != CatalogSort.NUMBER) {
                        androidx.compose.material3.TextButton(onClick = onClearFilters) { Text("Restablecer filtros") }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        when {
                            favoritesOnly -> "Tu colección"
                            query.isNotBlank() -> "Resultados de búsqueda"
                            else -> "Explora la Pokédex"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (state.catalogLoading) "Cargando catálogo…" else "${filtered.size} Pokémon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            when {
                state.catalogLoading -> item(span = { GridItemSpan(maxLineSpan) }) { CatalogMessage("Cargando Pokémon…", true) }
                state.catalogError != null -> item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CatalogMessage(state.catalogError, false)
                        Button(onClick = onLoad) { Text("Reintentar") }
                    }
                }
                filtered.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    CatalogMessage(
                        if (favoritesOnly && state.favorites.isEmpty()) "Aún no has capturado favoritos. Toca el corazón de una tarjeta."
                        else "No hay Pokémon con estos criterios.", false
                    )
                }
                else -> {
                    items(visible, key = { it.id }) { pokemon ->
                        PokemonTile(
                            pokemon = pokemon,
                            favorite = pokemon.id in state.favorites,
                            inTeam = pokemon.id in state.team,
                            onClick = { onPokemonClick(pokemon.id) },
                            onFavorite = { onFavorite(pokemon.id) },
                            onTeam = { onTeam(pokemon.id) }
                        )
                    }
                    if (filtered.size > visible.size) item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.Center) {
                            OutlinedButton(onClick = onShowMore) { Text("Mostrar más Pokémon") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> SelectorChip(
    label: String,
    selected: Boolean,
    options: List<Pair<String, T>>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(selected = selected, onClick = { expanded = true }, label = { Text(label) })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (name, value) ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    onSelect(value)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun PokemonTile(
    pokemon: NamedResource,
    favorite: Boolean,
    inTeam: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onTeam: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(start = 14.dp, end = 10.dp, top = 5.dp, bottom = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "N.º ${pokemon.id.toString().padStart(4, '0')}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (favorite) "Quitar de capturados" else "Guardar en capturados",
                        tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(122.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(104.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
                AsyncImage(
                    model = artworkUrl(pokemon.id),
                    contentDescription = "Ilustración de ${displayName(pokemon.name)}",
                    modifier = Modifier.size(116.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                displayName(pokemon.name),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ver ficha", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                IconButton(
                    onClick = onTeam,
                    modifier = Modifier.semantics {
                        contentDescription = if (inTeam) "Quitar del equipo" else "Añadir al equipo"
                    }
                ) {
                    PokeballMark(size = 22.dp,
                        color = if (inTeam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
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
        Text(message, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
