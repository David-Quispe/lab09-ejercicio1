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
    val abilities: List<PokemonAbilitySlot>,
    val moves: List<PokemonMoveSlot>
)

data class PokemonSprites(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("front_shiny") val frontShiny: String?,
    val other: OtherSprites?
)

data class OtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("front_shiny") val frontShiny: String?
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

data class PokemonMoveSlot(
    val move: NamedResource,
    @SerializedName("version_group_details") val versionGroupDetails: List<MoveVersionDetail>
)

data class MoveVersionDetail(
    @SerializedName("level_learned_at") val levelLearnedAt: Int,
    @SerializedName("move_learn_method") val moveLearnMethod: NamedResource,
    @SerializedName("version_group") val versionGroup: NamedResource
)

data class PokemonSpeciesResponse(
    val names: List<LocalizedName>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    @SerializedName("evolution_chain") val evolutionChain: ResourceUrl?,
    val generation: NamedResource?,
    @SerializedName("egg_groups") val eggGroups: List<NamedResource>?
)

data class ResourceUrl(val url: String) {
    val id: Int get() = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
}

data class LocalizedName(val name: String, val language: NamedResource)

data class FlavorTextEntry(
    @SerializedName("flavor_text") val flavorText: String,
    val language: NamedResource
)

data class PokemonProfile(
    val pokemon: PokemonResponse,
    val displayName: String,
    val description: String?,
    val evolutions: List<EvolutionStage> = emptyList(),
    val abilityDetails: List<AbilityDetail> = emptyList(),
    val moves: List<LearnedMove> = emptyList(),
    val moveVersion: String? = null
) {
    val artworkUrl: String?
        get() = pokemon.sprites.other?.officialArtwork?.frontDefault
            ?: pokemon.sprites.frontDefault

    val shinyUrl: String?
        get() = pokemon.sprites.other?.officialArtwork?.frontShiny
            ?: pokemon.sprites.frontShiny
}

data class EvolutionChainResponse(val chain: EvolutionNode)

data class EvolutionNode(
    val species: NamedResource,
    @SerializedName("evolution_details") val evolutionDetails: List<EvolutionDetail>,
    @SerializedName("evolves_to") val evolvesTo: List<EvolutionNode>
)

data class EvolutionDetail(
    val trigger: NamedResource?,
    val item: NamedResource?,
    @SerializedName("min_level") val minLevel: Int?,
    @SerializedName("min_happiness") val minHappiness: Int?,
    @SerializedName("held_item") val heldItem: NamedResource?,
    @SerializedName("time_of_day") val timeOfDay: String?,
    @SerializedName("known_move") val knownMove: NamedResource?,
    val location: NamedResource?
)

data class EvolutionStage(
    val speciesId: Int,
    val name: String,
    val requirement: String,
    val parentSpeciesId: Int? = null
)

data class AbilityResponse(
    val names: List<LocalizedName>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
    @SerializedName("effect_entries") val effectEntries: List<LocalizedEffect>
)

data class LocalizedEffect(
    @SerializedName("short_effect") val shortEffect: String,
    val language: NamedResource
)

data class AbilityDetail(val name: String, val description: String, val hidden: Boolean)

data class LearnedMove(val name: String, val method: String, val level: Int)

data class PokemonIndexEntry(
    val id: Int,
    val name: String,
    val species: Int,
    @SerializedName("default") val isDefault: Boolean,
    val generation: Int,
    val types: List<String>,
    val eggs: List<String>,
    val height: Int,
    val weight: Int,
    val attack: Int,
    val total: Int
)

fun artworkUrl(id: Int): String =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

fun displayName(name: String): String =
    name.replace('-', ' ').replaceFirstChar { it.uppercase() }
