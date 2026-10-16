package com.example.lab09_ejercicio1.data

import com.google.gson.annotations.SerializedName

data class PokemonListResponse(
    val count: Int,
    val results: List<NamedResource>
)

data class NamedResource(
    val name: String,
    val url: String
) {
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
}

data class PokemonResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val species: NamedResource,
    val sprites: PokemonSprites,
    val types: List<PokemonTypeSlot>,
    val stats: List<PokemonStat>,
    val abilities: List<PokemonAbilitySlot>
)

data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtherSprites?
)

data class OtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default") val frontDefault: String?
)

data class PokemonTypeSlot(val slot: Int, val type: NamedResource)

data class PokemonStat(
    @SerializedName("base_stat") val baseStat: Int,
    val stat: NamedResource
)

data class PokemonAbilitySlot(
    @SerializedName("is_hidden") val isHidden: Boolean,
    val ability: NamedResource
)

data class PokemonSpeciesResponse(
    val names: List<LocalizedName>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>
)

data class LocalizedName(val name: String, val language: NamedResource)

data class FlavorTextEntry(
    @SerializedName("flavor_text") val flavorText: String,
    val language: NamedResource
)

data class PokemonProfile(
    val pokemon: PokemonResponse,
    val displayName: String,
    val description: String?
) {
    val artworkUrl: String?
        get() = pokemon.sprites.other?.officialArtwork?.frontDefault
            ?: pokemon.sprites.frontDefault
}

fun artworkUrl(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

fun displayName(name: String): String =
    name.replace('-', ' ').replaceFirstChar { it.uppercase() }
