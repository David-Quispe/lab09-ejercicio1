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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lab09_ejercicio1.data.PokemonProfile
import com.example.lab09_ejercicio1.data.PokemonStat
import com.example.lab09_ejercicio1.data.displayName
import com.example.lab09_ejercicio1.ui.theme.pokemonTypeColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    id: Int,
    state: PokedexUiState,
    onLoad: (Int) -> Unit,
    onBack: () -> Unit
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
                ProfileContent(state.profile, innerPadding)
            }
        }
    }
}

@Composable
private fun ProfileContent(profile: PokemonProfile, innerPadding: PaddingValues) {
    val pokemon = profile.pokemon
    val accent = pokemonTypeColor(pokemon.types.firstOrNull()?.type?.name.orEmpty())

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
                            model = profile.artworkUrl,
                            contentDescription = "Ilustración de ${profile.displayName}",
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
                    pokemon.abilities.forEach { ability ->
                        Text(
                            if (ability.isHidden) {
                                "${displayName(ability.ability.name)} · oculta"
                            } else {
                                displayName(ability.ability.name)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
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
