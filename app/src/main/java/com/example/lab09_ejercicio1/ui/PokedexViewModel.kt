package com.example.lab09_ejercicio1.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab09_ejercicio1.data.NamedResource
import com.example.lab09_ejercicio1.data.PokemonProfile
import com.example.lab09_ejercicio1.data.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PokedexUiState(
    val catalog: List<NamedResource> = emptyList(),
    val query: String = "",
    val visibleCount: Int = 24,
    val catalogLoading: Boolean = false,
    val catalogError: String? = null,
    val profile: PokemonProfile? = null,
    val profileLoading: Boolean = false,
    val profileError: String? = null
)

class PokedexViewModel(
    private val repository: PokemonRepository = PokemonRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PokedexUiState())
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
                _uiState.update {
                    it.copy(
                        catalogLoading = false,
                        catalogError = "No se pudo cargar la Pokédex. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query, visibleCount = 24) }
    }

    fun showMore() {
        _uiState.update { it.copy(visibleCount = it.visibleCount + 24) }
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
