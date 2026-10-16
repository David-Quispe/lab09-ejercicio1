package com.example.lab09_ejercicio1.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApi {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 2000,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemon(@Path("id") id: Int): PokemonResponse

    @GET("pokemon-species/{name}")
    suspend fun getSpecies(@Path("name") name: String): PokemonSpeciesResponse

    @GET("evolution-chain/{id}")
    suspend fun getEvolutionChain(@Path("id") id: Int): EvolutionChainResponse

    @GET("ability/{id}")
    suspend fun getAbility(@Path("id") id: Int): AbilityResponse
}

// PokéAPI publica estos mismos recursos como archivos JSON en su repositorio api-data.
interface PokeStaticApi {
    @GET("pokemon/index.json")
    suspend fun getPokemonList(): PokemonListResponse

    @GET("pokemon/{id}/index.json")
    suspend fun getPokemon(@Path("id") id: Int): PokemonResponse

    @GET("pokemon-species/{id}/index.json")
    suspend fun getSpecies(@Path("id") id: Int): PokemonSpeciesResponse

    @GET("evolution-chain/{id}/index.json")
    suspend fun getEvolutionChain(@Path("id") id: Int): EvolutionChainResponse

    @GET("ability/{id}/index.json")
    suspend fun getAbility(@Path("id") id: Int): AbilityResponse
}
