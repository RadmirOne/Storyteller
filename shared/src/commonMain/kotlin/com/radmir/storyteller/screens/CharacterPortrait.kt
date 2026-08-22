package com.radmir.storyteller.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.radmir.storyteller.models.PlayerAppearance
import com.radmir.storyteller.models.PlayerOutfit
import com.radmir.storyteller.screens.scene.rememberResourceImageBitmap

@Composable
internal fun CharacterPortrait(appearance: PlayerAppearance, outfit: PlayerOutfit, modifier: Modifier,
    crop: PortraitCrop = PortraitCrop.FULL,
    transparent: Boolean = false) {
    val bitmap = rememberResourceImageBitmap(if (transparent)
        "files/characters/player/wardrobe-scene.png" else "files/characters/player/wardrobe-v2.png")
    val row = when (appearance) {
        PlayerAppearance.LIGHT_HAIR -> 0
        PlayerAppearance.DARK_HAIR -> 1
        PlayerAppearance.RED_HAIR -> 2
    }
    val column = when (outfit) {
        PlayerOutfit.JACKET -> 0
        PlayerOutfit.SWEATER -> 1
        PlayerOutfit.COAT -> 2
    }
    Canvas(modifier.semantics { contentDescription = "${appearance.label()}, ${outfit.label()}" }) {
        bitmap?.let { image ->
            val cellWidth = image.width / 3
            val cellHeight = image.height / 3
            // Crop each atlas cell independently, keeping the face and outfit undistorted.
            val regionHeight = (cellHeight * when (crop) {
                PortraitCrop.FULL -> 1f
                PortraitCrop.FACE -> .56f
                PortraitCrop.OUTFIT -> .52f
            }).toInt()
            val regionTop = if (crop == PortraitCrop.OUTFIT) (cellHeight * .35f).toInt() else 0
            val cropWidth = minOf(cellWidth, (regionHeight * size.width / size.height).toInt()).coerceAtLeast(1)
            val cropHeight = minOf(regionHeight, (cellWidth * size.height / size.width).toInt()).coerceAtLeast(1)
            drawImage(image,
                srcOffset = IntOffset(column * cellWidth + (cellWidth - cropWidth) / 2, row * cellHeight + regionTop),
                srcSize = IntSize(cropWidth, cropHeight),
                dstSize = IntSize(size.width.toInt(), size.height.toInt()))
        }
    }
}

internal enum class PortraitCrop { FULL, FACE, OUTFIT }

internal fun PlayerAppearance.label() = when (this) {
    PlayerAppearance.LIGHT_HAIR -> "Блондинка"
    PlayerAppearance.DARK_HAIR -> "Брюнетка"
    PlayerAppearance.RED_HAIR -> "Рыжая"
}

internal fun PlayerOutfit.label() = when (this) {
    PlayerOutfit.JACKET -> "Куртка"
    PlayerOutfit.SWEATER -> "Свитер"
    PlayerOutfit.COAT -> "Плащ"
}
