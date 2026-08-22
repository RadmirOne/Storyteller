package com.radmir.storyteller.screens.scene

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.radmir.storyteller.models.StageCharacter
import com.radmir.storyteller.models.StagePosition

@Composable
fun BoxScope.CharacterSprite(stage: StageCharacter, spriteResource: String?) {
    if (spriteResource == null || !stage.visible) return
    val bmp = rememberResourceImageBitmap(spriteResource) ?: return

    val alignment = when (stage.position) {
        StagePosition.LEFT -> Alignment.BottomStart
        StagePosition.CENTER -> Alignment.BottomCenter
        StagePosition.RIGHT -> Alignment.BottomEnd
    }

    val appear = remember(stage.characterId, stage.position) { Animatable(0f) }
    LaunchedEffect(stage.characterId, stage.position) {
        appear.animateTo(1f, tween(400))
    }

    Image(
        bitmap = bmp,
        contentDescription = stage.characterId,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .align(alignment)
            .fillMaxHeight(0.55f)
            .aspectRatio(bmp.width.toFloat() / bmp.height.toFloat())
            .graphicsLayer {
                scaleX = stage.scale
                scaleY = stage.scale
                alpha = appear.value
            }
    )
}
