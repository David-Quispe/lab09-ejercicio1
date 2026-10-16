package com.example.lab09_ejercicio1.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonRepository(
    private val api: PokeApi = Retrofit.Builder()
        .baseUrl("https://pokeapi.co/api/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PokeApi::class.java)
) {
    private var catalogCache: List<NamedResource>? = null
    private val profileCache = mutableMapOf<Int, PokemonProfile>()

    suspend fun getCatalog(): List<NamedResource> {
        catalogCache?.let { return it }
        return api.getPokemonList().results.also { catalogCache = it }
    }

    suspend fun getProfile(id: Int): PokemonProfile {
        profileCache[id]?.let { return it }
        val pokemon = api.getPokemon(id)
        val species = runCatching { api.getSpecies(pokemon.species.name) }.getOrNull()
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
}
