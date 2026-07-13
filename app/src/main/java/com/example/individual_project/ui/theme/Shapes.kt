package com.example.individual_project.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TmShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // badges, tags
    small      = RoundedCornerShape(8.dp),   // chips, small cards
    medium     = RoundedCornerShape(12.dp),  // text fields, buttons
    large      = RoundedCornerShape(16.dp),  // cards
    extraLarge = RoundedCornerShape(20.dp)   // featured cards, bottom sheets
)
