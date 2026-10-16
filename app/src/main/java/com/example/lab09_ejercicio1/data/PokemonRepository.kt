package com.example.lab09_ejercicio1.data

import kotlinx.coroutines.CancellationException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonRepository(
    private val api: PokeApi = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PokeApi::class.java),
    private val staticApi: PokeStaticApi = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/PokeAPI/api-data/master/data/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PokeStaticApi::class.java)
) {
    private var catalogCache: List<NamedResource>? = null
    private val profileCache = mutableMapOf<Int, PokemonProfile>()
    private var useStaticApi = false

    suspend fun getCatalog(): List<NamedResource> {
        catalogCache?.let { return it }
        return request(
            primary = { api.getPokemonList().results },
            backup = { staticApi.getPokemonList().results }
        ).also { catalogCache = it }
    }

    suspend fun getProfile(id: Int): PokemonProfile {
        profileCache[id]?.let { return it }
        val pokemon = request(
            primary = { api.getPokemon(id) },
            backup = { staticApi.getPokemon(id) }
        )
        val species = try {
            request(
                primary = { api.getSpecies(pokemon.species.name) },
                backup = { staticApi.getSpecies(pokemon.species.id) }
            )
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            null
        }
        val spanishName = species?.names?.firstOrNull { it.language.name == "es" }?.name
        val description = species?.flavorTextEntries
            ?.lastOrNull { it.language.name == "es" }
            ?.flavorText
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
        return PokemonProfile(
            pokemon = pokemon,
            displayName = spanishName ?: displayName(pokemon.name),
            description = description
        ).also { profileCache[id] = it }
    }

    private suspend fun <T> request(primary: suspend () -> T, backup: suspend () -> T): T {
        if (useStaticApi) return backup()
        return try {
            primary()
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            useStaticApi = true
            backup()
        }
    }
}
