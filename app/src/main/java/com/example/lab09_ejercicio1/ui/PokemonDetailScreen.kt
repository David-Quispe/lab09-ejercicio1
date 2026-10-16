package com.example.lab09_ejercicio1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lab09_ejercicio1.data.PokemonProfile
import com.example.lab09_ejercicio1.data.PokemonReferenceData
import com.example.lab09_ejercicio1.data.PokemonStat
import com.example.lab09_ejercicio1.data.artworkUrl
import com.example.lab09_ejercicio1.data.displayName
import com.example.lab09_ejercicio1.ui.theme.pokemonTypeColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    id: Int,
    state: PokedexUiState,
    reference: PokemonReferenceData,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLoad: (Int) -> Unit,
    onBack: () -> Unit,
    onFavorite: (Int) -> Unit,
    onTeam: (Int) -> Unit,
    onEvolutionClick: (Int) -> Unit
) {
    LaunchedEffect(id) { onLoad(id) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Ficha Pokémon", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al catálogo")
                    }
                },
                actions = { ThemeModeButton(darkTheme, onToggleTheme) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when {
            state.profileLoading || state.profile?.pokemon?.id != id && state.profileError == null -> {
                DetailMessage("Cargando ficha…", true, innerPadding)
            }
            state.profileError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.profileError, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onLoad(id) }) { Text("Reintentar") }
                }
            }
            state.profile != null -> {
                ProfileContent(
                    state.profile, innerPadding, reference,
                    state.profile.pokemon.id in state.favorites,
                    state.profile.pokemon.id in state.team,
                    onFavorite, onTeam, onEvolutionClick
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: PokemonProfile,
    innerPadding: PaddingValues,
    reference: PokemonReferenceData,
    favorite: Boolean,
    inTeam: Boolean,
    onFavorite: (Int) -> Unit,
    onTeam: (Int) -> Unit,
    onEvolutionClick: (Int) -> Unit
) {
    val pokemon = profile.pokemon
    val accent = pokemonTypeColor(pokemon.types.firstOrNull()?.type?.name.orEmpty())
    var shiny by remember(pokemon.id) { mutableStateOf(false) }
    var showAllMoves by remember(pokemon.id) { mutableStateOf(false) }
    val incoming = reference.types.map { attack ->
        attack to reference.damage(attack, pokemon.types.map { it.type.name })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text(
                        "N.º ${pokemon.id.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Box(
                        modifier = Modifier.fillMaxWidth().height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier.size(210.dp).background(accent.copy(alpha = 0.22f), CircleShape)
                        )
                        AsyncImage(
                            model = if (shiny) profile.shinyUrl ?: profile.artworkUrl else profile.artworkUrl,
                            contentDescription = "${if (shiny) "Forma variocolor" else "Ilustración"} de ${profile.displayName}",
                            modifier = Modifier.size(220.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        profile.displayName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pokemon.types.sortedBy { it.slot }.forEach { slot ->
                            Surface(
                                color = pokemonTypeColor(slot.type.name).copy(alpha = 0.23f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    typeName(slot.type.name),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = shiny,
                            onClick = { shiny = !shiny },
                            enabled = profile.shinyUrl != null,
                            label = { Text(if (shiny) "Variocolor activo" else "Ver variocolor") }
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onFavorite(pokemon.id) }) {
                            Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (favorite) "Quitar de capturados" else "Guardar en capturados")
                        }
                        IconButton(
                            onClick = { onTeam(pokemon.id) },
                            modifier = Modifier.semantics {
                                contentDescription = if (inTeam) "Quitar del equipo" else "Añadir al equipo"
                            }
                        ) {
                            PokeballMark(size = 24.dp,
                                color = if (inTeam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Acerca de", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        profile.description ?: "Aún no hay una descripción en español para este Pokémon.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Debilidades y resistencias", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Daño recibido según los tipos actuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    EffectivenessRow("Débil", incoming.filter { it.second > 1.0 })
                    EffectivenessRow("Resiste", incoming.filter { it.second in 0.01..0.99 })
                    EffectivenessRow("Inmune", incoming.filter { it.second == 0.0 })
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementCard(
                    label = "Altura",
                    value = String.format(Locale.forLanguageTag("es-PE"), "%.1f m", pokemon.height / 10f),
                    modifier = Modifier.weight(1f)
                )
                MeasurementCard(
                    label = "Peso",
                    value = String.format(Locale.forLanguageTag("es-PE"), "%.1f kg", pokemon.weight / 10f),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Estadísticas base", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(18.dp))
                    pokemon.stats.forEach { stat ->
                        StatRow(stat, accent)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Habilidades", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    profile.abilityDetails.forEach { ability ->
                        Text(
                            ability.name + if (ability.hidden) " · oculta" else "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(ability.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Movimientos", style = MaterialTheme.typography.titleLarge)
                    Text("Por nivel o MT · ${profile.moveVersion?.let(::versionName) ?: "versión disponible"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    val shown = if (showAllMoves) profile.moves else profile.moves.take(16)
                    shown.forEach { move ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                            Text(move.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text(if (move.method == "level-up") "Nv. ${move.level}" else "MT",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (profile.moves.size > 16) TextButton(onClick = { showAllMoves = !showAllMoves }) {
                        Text(if (showAllMoves) "Ver menos" else "Ver los ${profile.moves.size} movimientos")
                    }
                }
            }
        }

        if (profile.evolutions.isNotEmpty()) item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text("Línea evolutiva", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        profile.evolutions.forEachIndexed { index, stage ->
                            if (index > 0 && stage.parentSpeciesId == profile.evolutions[index - 1].speciesId) {
                                Text("→", style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                            val targetId = reference.defaultPokemonForSpecies(stage.speciesId)
                            Column(
                                modifier = Modifier.width(128.dp)
                                    .clickable(enabled = targetId != null) { targetId?.let(onEvolutionClick) },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = targetId?.let(::artworkUrl),
                                    contentDescription = stage.name,
                                    modifier = Modifier.size(105.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(stage.name, style = MaterialTheme.typography.labelLarge)
                                Text(stage.requirement, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectivenessRow(label: String, values: List<Pair<String, Double>>) {
    if (values.isEmpty()) return
    Text(label, style = MaterialTheme.typography.titleMedium)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (type, multiplier) ->
            Surface(
                color = when (label) {
                    "Débil" -> MaterialTheme.colorScheme.errorContainer
                    "Resiste" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("${typeName(type)} ×${multiplier.toString().removeSuffix(".0")}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun MeasurementCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun StatRow(stat: PokemonStat, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            statName(stat.stat.name),
            modifier = Modifier.width(86.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { (stat.baseStat / 180f).coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(8.dp),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            stat.baseStat.toString(),
            modifier = Modifier.width(40.dp).padding(start = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailMessage(message: String, loading: Boolean, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
