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
}
