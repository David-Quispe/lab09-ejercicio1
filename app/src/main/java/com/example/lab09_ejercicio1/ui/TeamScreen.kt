package com.example.lab09_ejercicio1.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lab09_ejercicio1.data.PokemonReferenceData
import com.example.lab09_ejercicio1.data.artworkUrl
import com.example.lab09_ejercicio1.data.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    state: PokedexUiState,
    reference: PokemonReferenceData,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onExplore: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    val members = state.team.mapNotNull(reference.index::get)
    val weak = reference.types.map { type ->
        type to members.count { reference.damage(type, it.types) > 1.0 }
    }.filter { it.second > 0 }.sortedByDescending { it.second }
    val resistant = reference.types.map { type ->
        type to members.count { reference.damage(type, it.types) < 1.0 }
    }.filter { it.second > 0 }.sortedByDescending { it.second }
    val attackTypes = members.flatMap { it.types }.distinct()
    val covered = reference.types.filter { defender ->
        attackTypes.any { attack -> reference.damage(attack, listOf(defender)) > 1.0 }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi equipo", style = MaterialTheme.typography.titleLarge) },
                actions = { ThemeModeButton(darkTheme, onToggleTheme) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("Tu escuadrón", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("${members.size} de 6 Pokémon · añade o quita desde el catálogo")
                    }
                }
            }
            if (members.isEmpty()) item {
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Text("Empieza tu equipo", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Toca la Pokébola de cualquier tarjeta para añadirlo. Puedes elegir hasta seis.")
                        Spacer(Modifier.height(14.dp))
                        Button(onClick = onExplore) { Text("Explorar Pokémon") }
                    }
                }
            }
            items(members.size) { position ->
                val member = members[position]
                val resource = state.catalog.firstOrNull { it.id == member.id }
                Card(onClick = { onPokemonClick(member.id) }, shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = artworkUrl(member.id),
                            contentDescription = null,
                            modifier = Modifier.size(76.dp),
                            contentScale = ContentScale.Fit
                        )
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text(displayName(resource?.name ?: member.name),
                                style = MaterialTheme.typography.titleMedium)
                            Text(member.types.joinToString(" · ", transform = ::typeName),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onRemove(member.id) }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar del equipo")
                        }
                    }
                }
            }
            if (members.isNotEmpty()) {
                item { AnalysisCard("Puntos débiles", "Miembros que reciben daño aumentado", weak) }
                item { AnalysisCard("Resistencias", "Miembros que reciben daño reducido o nulo", resistant) }
                item {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) {
                            Text("Cobertura ofensiva", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("${covered.size} de 18 tipos pueden recibir daño supereficaz de los tipos de tu equipo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                covered.forEach { type -> TypeLabel(typeName(type)) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("La cobertura considera tipos, no los movimientos equipados.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(title: String, explanation: String, values: List<Pair<String, Int>>) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(explanation, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                values.take(10).forEach { (type, count) ->
                    TypeLabel("${typeName(type)} · $count")
                }
            }
        }
    }
}

@Composable
private fun TypeLabel(label: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge)
    }
}
