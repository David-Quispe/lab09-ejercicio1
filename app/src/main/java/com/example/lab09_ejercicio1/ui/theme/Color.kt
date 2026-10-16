package com.example.lab09_ejercicio1.ui.theme

import androidx.compose.ui.graphics.Color

val RojoPokedex = Color(0xFFD4364C)
val RojoOscuro = Color(0xFF9F2338)
val AzulTinta = Color(0xFF172638)
val AzulSuave = Color(0xFF5B6C7E)
val FondoClaro = Color(0xFFF5F7FA)
val AmarilloLuz = Color(0xFFFFD45D)

val FondoNoche = Color(0xFF111925)
val SuperficieNoche = Color(0xFF1D2A3B)
val RojoNoche = Color(0xFFFF8B9A)

fun pokemonTypeColor(type: String): Color = when (type) {
    "grass" -> Color(0xFF48A979)
    "fire" -> Color(0xFFE7794A)
    "water" -> Color(0xFF4D8DCD)
    "electric" -> Color(0xFFD9A829)
    "poison" -> Color(0xFF9A6AB1)
    "bug" -> Color(0xFF83A64A)
    "flying" -> Color(0xFF819FC7)
    "ground" -> Color(0xFFC39261)
    "rock" -> Color(0xFF9C8E72)
    "psychic" -> Color(0xFFD879A4)
    "ice" -> Color(0xFF67B7C4)
    "dragon" -> Color(0xFF6A74B6)
    "dark" -> Color(0xFF596475)
    "fairy" -> Color(0xFFD98DB3)
    "fighting" -> Color(0xFFBF6758)
    "ghost" -> Color(0xFF7775A5)
    "steel" -> Color(0xFF8298A5)
    else -> Color(0xFF85969E)
}
