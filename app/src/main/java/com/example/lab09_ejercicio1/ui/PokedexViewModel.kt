package com.example.lab09_ejercicio1.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab09_ejercicio1.data.NamedResource
import com.example.lab09_ejercicio1.data.PokemonProfile
import com.example.lab09_ejercicio1.data.PokemonReferenceData
import com.example.lab09_ejercicio1.data.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class CatalogSort(val label: String) {
    NUMBER("Número"), ATTACK("Mayor ataque"), WEIGHT("Más pesado"),
    HEIGHT("Más alto"), TOTAL("Más estadísticas")
}

data class PokedexUiState(
    val catalog: List<NamedResource> = emptyList(),
    val query: String = "",
    val visibleCount: Int = 24,
    val generation: Int? = null,
    val type: String? = null,
    val eggGroup: String? = null,
    val sort: CatalogSort = CatalogSort.NUMBER,
    val favorites: Set<Int> = emptySet(),
    val team: List<Int> = emptyList(),
    val notice: String? = null,
    val catalogLoading: Boolean = false,
    val catalogError: String? = null,
    val profile: PokemonProfile? = null,
    val profileLoading: Boolean = false,
    val profileError: String? = null
)

class PokedexViewModel(application: Application) : AndroidViewModel(application) {
    val reference = PokemonReferenceData(application)
    private val repository = PokemonRepository(reference.moveNames, reference.itemNames)
    private val preferences = application.getSharedPreferences("pokedex_usuario", 0)
    private val _uiState = MutableStateFlow(
        PokedexUiState(
            favorites = preferences.getStringSet("favoritos", emptySet()).orEmpty()
                .mapNotNull(String::toIntOrNull).toSet(),
            team = preferences.getString("equipo", "").orEmpty().split(',')
                .mapNotNull(String::toIntOrNull).take(6)
        )
    )
    val uiState = _uiState.asStateFlow()
    private var profileJob: Job? = null

    fun loadCatalog() {
        if (_uiState.value.catalog.isNotEmpty() || _uiState.value.catalogLoading) return
        _uiState.update { it.copy(catalogLoading = true, catalogError = null) }
        viewModelScope.launch {
            try {
                val catalog = repository.getCatalog()
                _uiState.update { it.copy(catalog = catalog, catalogLoading = false) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.e("Pokedex", "Error al cargar el catálogo", error)
                val local = reference.index.values.sortedBy { it.id }.map {
                    NamedResource(it.name, "/api/v2/pokemon/${it.id}/")
                }
                _uiState.update {
                    it.copy(
                        catalog = local,
                        catalogLoading = false,
                        notice = "Mostrando catálogo local. Las fichas necesitan conexión."
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) = _uiState.update { it.copy(query = query, visibleCount = 24) }
    fun showMore() = _uiState.update { it.copy(visibleCount = it.visibleCount + 24) }
    fun setGeneration(value: Int?) = _uiState.update { it.copy(generation = value, visibleCount = 24) }
    fun setType(value: String?) = _uiState.update { it.copy(type = value, visibleCount = 24) }
    fun setEggGroup(value: String?) = _uiState.update { it.copy(eggGroup = value, visibleCount = 24) }
    fun setSort(value: CatalogSort) = _uiState.update { it.copy(sort = value, visibleCount = 24) }
    fun clearFilters() = _uiState.update {
        it.copy(generation = null, type = null, eggGroup = null, sort = CatalogSort.NUMBER, visibleCount = 24)
    }
    fun clearNotice() = _uiState.update { it.copy(notice = null) }

    fun toggleFavorite(id: Int) {
        val next = _uiState.value.favorites.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        preferences.edit().putStringSet("favoritos", next.map(Int::toString).toSet()).apply()
        _uiState.update { it.copy(favorites = next) }
    }

    fun toggleTeam(id: Int) {
        val current = _uiState.value.team
        if (id !in current && current.size >= 6) {
            _uiState.update { it.copy(notice = "Tu equipo ya tiene seis Pokémon.") }
            return
        }
        val next = if (id in current) current - id else current + id
        preferences.edit().putString("equipo", next.joinToString(",")).apply()
        _uiState.update { it.copy(team = next, notice = null) }
    }

    fun loadProfile(id: Int) {
        if (_uiState.value.profile?.pokemon?.id == id) return
        profileJob?.cancel()
        _uiState.update { it.copy(profile = null, profileLoading = true, profileError = null) }
        profileJob = viewModelScope.launch {
            try {
                val profile = repository.getProfile(id)
                _uiState.update { it.copy(profile = profile, profileLoading = false) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                Log.e("Pokedex", "Error al cargar el Pokémon $id", error)
                val message = if (error is HttpException && error.code() == 404) {
                    "No encontramos ese Pokémon."
                } else {
                    "No se pudo cargar la ficha. Revisa tu conexión e inténtalo de nuevo."
                }
                _uiState.update { it.copy(profileLoading = false, profileError = message) }
            }
        }
    }
}
