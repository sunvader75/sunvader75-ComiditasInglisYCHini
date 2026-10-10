package com.comiditas.familia.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

object FoodSpacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val inset = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val screen = 16.dp
    val minimumTouchTarget = 48.dp
}
