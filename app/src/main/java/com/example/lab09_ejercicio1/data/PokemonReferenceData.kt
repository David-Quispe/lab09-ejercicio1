package com.example.lab09_ejercicio1.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Compact metadata derived from PokeAPI/pokeapi CSV and current type resources. */
class PokemonReferenceData(context: Context) {
    private val gson = Gson()
    val index: Map<Int, PokemonIndexEntry> = context.assets.open("pokemon_index.json").bufferedReader().use {
        gson.fromJson(it, Array<PokemonIndexEntry>::class.java).associateBy(PokemonIndexEntry::id)
    }
    val types: List<String> = listOf(
        "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground",
        "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"
    )
    private val chart: Map<String, Map<String, Double>> = context.assets.open("type_chart.json")
        .bufferedReader().use {
            val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
            gson.fromJson(it, type)
        }

    fun damage(attackType: String, defenderTypes: List<String>): Double =
        defenderTypes.fold(1.0) { result, defense ->
            result * (chart[attackType]?.get(defense) ?: 1.0)
        }

    fun defaultPokemonForSpecies(speciesId: Int): Int? =
        index.values.firstOrNull { it.species == speciesId && it.isDefault }?.id
}
