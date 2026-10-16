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
    private val abilityCache = mutableMapOf<Int, AbilityResponse>()
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
        val evolutions = optional {
            species?.evolutionChain?.id?.takeIf { it > 0 }?.let { chainId ->
                val chain = request(
                    primary = { api.getEvolutionChain(chainId) },
                    backup = { staticApi.getEvolutionChain(chainId) }
                )
                flattenEvolution(chain.chain)
            }
        }.orEmpty()
        val abilityDetails = pokemon.abilities.map { slot ->
            val detail = optional {
                val abilityId = slot.ability.id
                abilityCache[abilityId] ?: request(
                    primary = { api.getAbility(abilityId) },
                    backup = { staticApi.getAbility(abilityId) }
                ).also { abilityCache[abilityId] = it }
            }
            AbilityDetail(
                name = detail?.names?.firstOrNull { it.language.name == "es" }?.name
                    ?: displayName(slot.ability.name),
                description = detail?.flavorTextEntries?.lastOrNull { it.language.name == "es" }
                    ?.flavorText?.replace(Regex("\\s+"), " ")?.trim()
                    ?: detail?.effectEntries?.firstOrNull { it.language.name == "es" }
                        ?.shortEffect?.replace(Regex("\\s+"), " ")?.trim()
                    ?: "Sin descripción disponible en español.",
                hidden = slot.isHidden
            )
        }
        val version = if (pokemon.moves.any { slot ->
                slot.versionGroupDetails.any { it.versionGroup.name == "scarlet-violet" }
            }) "scarlet-violet" else pokemon.moves
                .flatMap { it.versionGroupDetails }
                .groupingBy { it.versionGroup.name }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key
        val moves = pokemon.moves.mapNotNull { slot ->
            val detail = slot.versionGroupDetails.firstOrNull {
                it.versionGroup.name == version && it.moveLearnMethod.name in setOf("level-up", "machine")
            } ?: return@mapNotNull null
            LearnedMove(displayName(slot.move.name), detail.moveLearnMethod.name, detail.levelLearnedAt)
        }.sortedWith(compareBy<LearnedMove> { it.method != "level-up" }.thenBy { it.level }.thenBy { it.name })
        return PokemonProfile(
            pokemon = pokemon,
            displayName = spanishName ?: displayName(pokemon.name),
            description = description,
            evolutions = evolutions,
            abilityDetails = abilityDetails,
            moves = moves,
            moveVersion = version
        ).also { profileCache[id] = it }
    }

    private suspend fun <T> optional(block: suspend () -> T): T? = try {
        block()
    } catch (error: Exception) {
        if (error is CancellationException) throw error
        null
    }

    private fun flattenEvolution(root: EvolutionNode): List<EvolutionStage> {
        val result = mutableListOf<EvolutionStage>()
        fun visit(node: EvolutionNode) {
            result += EvolutionStage(
                speciesId = node.species.id,
                name = displayName(node.species.name),
                requirement = node.evolutionDetails.firstOrNull()?.let(::evolutionRequirement) ?: "Origen"
            )
            node.evolvesTo.forEach(::visit)
        }
        visit(root)
        return result
    }

    private fun evolutionRequirement(detail: EvolutionDetail): String = when {
        detail.item != null -> "Usar ${displayName(detail.item.name)}"
        detail.minLevel != null -> "Nivel ${detail.minLevel}"
        detail.trigger?.name == "trade" -> "Intercambio" +
            (detail.heldItem?.let { " con ${displayName(it.name)}" } ?: "")
        detail.minHappiness != null -> "Amistad alta"
        detail.knownMove != null -> "Con ${displayName(detail.knownMove.name)}"
        detail.location != null -> "En ${displayName(detail.location.name)}"
        !detail.timeOfDay.isNullOrBlank() -> "Durante ${if (detail.timeOfDay == "day") "el día" else "la noche"}"
        else -> displayName(detail.trigger?.name ?: "Condición especial")
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
