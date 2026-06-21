package com.example.individual_project.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 8-point grid spacing system.
 * All padding, margin, and gap values must use these constants.
 * No raw .dp literals outside of this file and Shapes.kt.
 */
object Spacing {
    val xxs = 2.dp
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp

    // Semantic aliases
    val screenHorizontal = 20.dp   // page-level horizontal padding
    val cardPadding      = 16.dp   // inner card padding
    val itemGap          = 8.dp    // gap between list items
    val sectionGap       = 24.dp   // gap between page sections
    val headerPaddingTop = 48.dp   // status-bar-aware header top padding

    // Component sizes
    val iconSm          = 16.dp
    val iconMd          = 20.dp
    val iconLg          = 24.dp
    val avatarSm        = 38.dp
    val avatarMd        = 48.dp
    val avatarLg        = 84.dp
    val thumbnailSize   = 64.dp
    val buttonHeight    = 54.dp
    val bottomNavHeight = 64.dp
}
