package com.radmir.storyteller.screens.scene

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.radmir.storyteller.models.PlayerCharacter
import com.radmir.storyteller.models.PlayerAppearance
import com.radmir.storyteller.models.PlayerOutfit
import com.radmir.storyteller.models.CharacterAppearance
import com.radmir.storyteller.models.StagePosition

/** Dialogue framing is independent of the background camera and world-stage scale. */
@Composable
internal fun DialogueCharacter(
    speakerName: String,
    spriteResource: String?,
    player: PlayerCharacter?,
    position: StagePosition,
    appearance: CharacterAppearance,
    modifier: Modifier = Modifier
) {
    val path = if (player != null) "files/characters/player/wardrobe-scene.png" else spriteResource
    if (path == null) return
    val bitmap = rememberResourceImageBitmap(path) ?: return
    val progress = remember(appearance) {
        Animatable(if (appearance.enabled && appearance.durationMs > 0) 0f else 1f)
    }
    LaunchedEffect(progress) {
        if (progress.value < 1f) progress.animateTo(1f, tween(appearance.durationMs))
    }
    val source = remember(bitmap, player?.appearance, player?.outfit) {
        val column = when (player?.outfit) {
            PlayerOutfit.SWEATER -> 1
            PlayerOutfit.COAT -> 2
            else -> 0
        }
        val row = when (player?.appearance) {
            PlayerAppearance.DARK_HAIR -> 1
            PlayerAppearance.RED_HAIR -> 2
            else -> 0
        }
        val width = if (player != null) bitmap.width / 3 else bitmap.width
        val height = if (player != null) bitmap.height / 3 else bitmap.height
        val originX = column * width
        val originY = row * height
        val pixels = bitmap.toPixelMap()
        var left = width
        var right = 0
        var top = height
        var bottom = 0
        // Ignore transparent padding so every actor has the same headroom and framing.
        for (y in 0 until height step 2) for (x in 0 until width step 2) {
            if (pixels[originX + x, originY + y].alpha > .15f) {
                left = minOf(left, x); right = maxOf(right, x)
                top = minOf(top, y); bottom = maxOf(bottom, y)
            }
        }
        if (right <= left || bottom <= top) {
            DialogueCrop(IntOffset(originX, originY), IntSize(width, height))
        } else {
            left = (left - 3).coerceAtLeast(0)
            right = (right + 4).coerceAtMost(width)
            top = (top - 3).coerceAtLeast(0)
            bottom = (bottom + 4).coerceAtMost(height)
            // NPC sprites are full length; player atlas cells already end at the thighs.
            val framedHeight = if (player == null) ((bottom - top) * .64f).toInt() else bottom - top
            DialogueCrop(IntOffset(originX + left, originY + top), IntSize(right - left, framedHeight))
        }
    }
    Canvas(modifier.clipToBounds().semantics { contentDescription = speakerName }) {
        val scale = minOf(size.height * .92f / source.size.height, size.width * .84f / source.size.width)
        val width = (source.size.width * scale).toInt().coerceAtLeast(1)
        val height = (source.size.height * scale).toInt().coerceAtLeast(1)
        val inset = size.width * .025f
        val x = when (position) {
            StagePosition.LEFT -> inset
            StagePosition.CENTER -> (size.width - width) / 2f
            StagePosition.RIGHT -> size.width - width - inset
        }
        val direction = when (position) {
            StagePosition.LEFT -> -1f
            StagePosition.CENTER -> 0f
            StagePosition.RIGHT -> 1f
        }
        val offset = direction * size.width * appearance.slideDistance * (1f - progress.value)
        drawImage(bitmap, srcOffset = source.offset, srcSize = source.size,
            dstOffset = IntOffset((x + offset).toInt(), (size.height - height).toInt()),
            dstSize = IntSize(width, height),
            alpha = if (appearance.fade) progress.value else 1f)
    }
}

private data class DialogueCrop(val offset: IntOffset, val size: IntSize)
