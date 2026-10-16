package com.example.lab09_ejercicio1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lab09_ejercicio1.data.displayName

@Composable
fun PokeballMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = Color.White
) {
    Canvas(modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val stroke = this.size.width * 0.09f
        drawCircle(color, style = Stroke(stroke))
        drawLine(
            color = color,
            start = Offset(stroke / 2, center.y),
            end = Offset(this.size.width - stroke / 2, center.y),
            strokeWidth = stroke
        )
        drawCircle(color, radius = this.size.width * 0.18f)
        drawCircle(color = Color.White, radius = this.size.width * 0.09f)
    }
}

fun typeName(type: String): String = when (type) {
    "normal" -> "Normal"
    "fire" -> "Fuego"
    "water" -> "Agua"
    "electric" -> "Eléctrico"
    "grass" -> "Planta"
    "ice" -> "Hielo"
    "fighting" -> "Lucha"
    "poison" -> "Veneno"
    "ground" -> "Tierra"
    "flying" -> "Volador"
    "psychic" -> "Psíquico"
    "bug" -> "Bicho"
    "rock" -> "Roca"
    "ghost" -> "Fantasma"
    "dragon" -> "Dragón"
    "dark" -> "Siniestro"
    "steel" -> "Acero"
    "fairy" -> "Hada"
    else -> displayName(type)
}

fun statName(stat: String): String = when (stat) {
    "hp" -> "PS"
    "attack" -> "Ataque"
    "defense" -> "Defensa"
    "special-attack" -> "At. esp."
    "special-defense" -> "Def. esp."
    "speed" -> "Velocidad"
    else -> displayName(stat)
}
