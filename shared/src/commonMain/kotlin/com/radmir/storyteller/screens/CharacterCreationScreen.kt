package com.radmir.storyteller.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radmir.storyteller.models.PlayerAppearance
import com.radmir.storyteller.models.PlayerCharacter
import com.radmir.storyteller.models.PlayerOutfit
import com.radmir.storyteller.screens.scene.rememberResourceImageBitmap

private val Gold = Color(0xFFE4C58D)
private val Ink = Color(0xFF10252E)
private val Muted = Color(0xFFA5B7BC)
private val Paper = Color(0xFFF6EEE0)

@Composable
fun CharacterCreationScreen(
    onCharacterCreated: (PlayerCharacter) -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var appearance by rememberSaveable { mutableStateOf(PlayerAppearance.DARK_HAIR) }
    var outfit by rememberSaveable { mutableStateOf(PlayerOutfit.JACKET) }
    var step by rememberSaveable { mutableStateOf(0) }
    val normalizedName = name.trim()
    val scroll = rememberScrollState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(step) {
        focusManager.clearFocus()
        scroll.scrollTo(0)
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF203B44), Color(0xFF08161E)))
        ).imePadding().padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().fillMaxHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Ink,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = .22f))
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { if (step > 0) step-- else onBack() }) {
                        Text(if (step > 0) "← Назад" else "← К историям", color = Gold)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("ВАША ГЕРОИНЯ", color = Muted, fontSize = 10.sp,
                        letterSpacing = 2.sp, modifier = Modifier.padding(end = 12.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp).selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Имя", "Внешность", "Одежда").forEachIndexed { index, title ->
                        Column(Modifier.weight(1f).selectable(
                            selected = index == step,
                            enabled = index < step,
                            role = Role.Tab,
                            onClick = { step = index }
                        )) {
                            Box(Modifier.fillMaxWidth().height(2.dp)
                                .background(if (index <= step) Gold else Color(0xFF344850)))
                            Text("0${index + 1}  $title", fontSize = 11.sp,
                                color = if (index == step) Gold else Muted,
                                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
                        }
                    }
                }
                AnimatedContent(
                    targetState = step,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = { fadeIn(tween(240, delayMillis = 90)) togetherWith fadeOut(tween(90)) },
                    label = "Шаг создания героини"
                ) { currentStep ->
                BoxWithConstraints(Modifier.fillMaxSize()) {
                val portraitSize = (maxHeight - 220.dp).coerceIn(100.dp, 300.dp)
                val compact = maxHeight < 500.dp
                Column(
                    Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when (currentStep) { 0 -> "Как вас зовут?"; 1 -> "Выберите свою героиню"; else -> "Последний штрих" },
                        fontFamily = FontFamily.Serif, fontSize = if (compact) 24.sp else 28.sp, lineHeight = 30.sp,
                        color = Paper, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    if (currentStep == 0) Text(
                        when (currentStep) {
                            0 -> "У каждой истории есть имя. У этой — ваше."
                            1 -> "Выберите внешность, близкую вам."
                            else -> "Примерьте образ для первой главы."
                        },
                        color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                    )
                    if (currentStep == 0) {
                        val backdrop = rememberResourceImageBitmap("files/scenes/island_arrival.png")
                        Box(Modifier.fillMaxWidth().height(if (compact) 100.dp else 180.dp).clip(RoundedCornerShape(20.dp))) {
                            backdrop?.let {
                                Image(it, contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize())
                            }
                            Box(Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent, Ink.copy(alpha = .8f)))))
                            Text("Первая глава начинается с вас", color = Paper,
                                fontFamily = FontFamily.Serif, fontSize = 19.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp))
                        }
                    }
                    Spacer(Modifier.height(if (currentStep == 0) 20.dp else 12.dp))
                    when (currentStep) {
                        0 -> OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(24) },
                            label = { Text("Имя героини") },
                            placeholder = { Text("Например, Алиса") },
                            supportingText = { Text("Имя вашей героини · ${name.length}/24") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { if (normalizedName.isNotEmpty()) step = 1 }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Paper, unfocusedTextColor = Paper,
                                focusedBorderColor = Gold, unfocusedBorderColor = Muted,
                                focusedLabelColor = Gold, unfocusedLabelColor = Muted,
                                cursorColor = Gold, focusedSupportingTextColor = Muted,
                                unfocusedSupportingTextColor = Muted
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        1 -> CharacterSelector(
                            name = normalizedName,
                            appearance = appearance,
                            portraitSize = portraitSize,
                            onAppearanceSelected = { appearance = it }
                        )
                        else -> CharacterSelector(
                            name = normalizedName,
                            appearance = appearance,
                            outfit = outfit,
                            portraitSize = portraitSize,
                            choosingOutfit = true,
                            onAppearanceSelected = { appearance = it },
                            onOutfitSelected = { outfit = it }
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
                }
                }
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Button(
                        onClick = {
                            if (step < 2) step++
                            else onCharacterCreated(PlayerCharacter(normalizedName, appearance, outfit))
                        },
                        enabled = normalizedName.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text(when (step) { 0 -> "Выбрать внешность  →"; 1 -> "Перейти к одежде  →"; else -> "Начать историю · $normalizedName  →" },
                            textAlign = TextAlign.Center)
                    }
                    Text("${step + 1} из 3 · ${if (step == 2) "Всё готово к первой главе" else "Ваша история начинается здесь"}",
                        color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacterSelector(
    name: String,
    appearance: PlayerAppearance,
    onAppearanceSelected: (PlayerAppearance) -> Unit,
    portraitSize: Dp,
    outfit: PlayerOutfit = PlayerOutfit.JACKET,
    choosingOutfit: Boolean = false,
    onOutfitSelected: (PlayerOutfit) -> Unit = {}
) {
    val options = PlayerAppearance.entries
    val outfits = PlayerOutfit.entries
    val selectedIndex = if (choosingOutfit) outfits.indexOf(outfit) else options.indexOf(appearance)
    val onSelected: (Int) -> Unit = { index ->
        if (choosingOutfit) onOutfitSelected(outfits[index]) else onAppearanceSelected(options[index])
    }
    val currentIndex by rememberUpdatedState(selectedIndex)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val swipeThreshold = with(LocalDensity.current) { 40.dp.toPx() }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.widthIn(max = 300.dp).fillMaxWidth().height(portraitSize)
                .clip(RoundedCornerShape(20.dp))
                .pointerInput(swipeThreshold) {
                    var distance = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { distance = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            distance += amount
                        },
                        onDragCancel = { distance = 0f },
                        onDragEnd = {
                            val direction = when {
                                distance < -swipeThreshold -> 1
                                distance > swipeThreshold -> -1
                                else -> 0
                            }
                            if (direction != 0) {
                                currentOnSelected((currentIndex + direction + options.size) % options.size)
                            }
                        }
                    )
                }
        ) {
            Crossfade(selectedIndex, modifier = Modifier.size(portraitSize).align(Alignment.Center)
                .clip(RoundedCornerShape(20.dp)), animationSpec = tween(240), label = "Образ героини") { index ->
                CharacterPortrait(if (choosingOutfit) appearance else options[index],
                    if (choosingOutfit) outfits[index] else PlayerOutfit.JACKET,
                    Modifier.fillMaxSize(), if (choosingOutfit) PortraitCrop.FULL else PortraitCrop.FACE)
            }
            IconButton(
                onClick = { onSelected((selectedIndex + options.size - 1) % options.size) },
                modifier = Modifier.align(Alignment.CenterStart).padding(4.dp)
                    .background(Ink.copy(alpha = .75f), RoundedCornerShape(24.dp))
                    .semantics { contentDescription = if (choosingOutfit) "Предыдущий наряд" else "Предыдущая внешность" }
            ) { Text("‹", color = Gold, fontSize = 32.sp) }
            IconButton(
                onClick = { onSelected((selectedIndex + 1) % options.size) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(4.dp)
                    .background(Ink.copy(alpha = .75f), RoundedCornerShape(24.dp))
                    .semantics { contentDescription = if (choosingOutfit) "Следующий наряд" else "Следующая внешность" }
            ) { Text("›", color = Gold, fontSize = 32.sp) }
        }
        Text(name, color = Paper, fontFamily = FontFamily.Serif, fontSize = if (portraitSize < 180.dp) 20.sp else 23.sp,
            textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
        Row(Modifier.widthIn(max = if (portraitSize < 180.dp) 220.dp else 360.dp).fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (choosingOutfit) outfits.forEach { option ->
                PortraitOption(option.label(), option == outfit, { onOutfitSelected(option) },
                    appearance, option, Modifier.weight(1f))
            } else options.forEach { option ->
                PortraitOption(option.label(), option == appearance, { onAppearanceSelected(option) },
                    option, PlayerOutfit.JACKET, Modifier.weight(1f), portrait = true)
            }
        }
    }
}

@Composable
private fun PortraitOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    appearance: PlayerAppearance,
    outfit: PlayerOutfit,
    modifier: Modifier = Modifier,
    portrait: Boolean = false
) {
    val borderColor by animateColorAsState(if (selected) Gold else Color(0xFF344850),
        animationSpec = tween(180), label = "Выбор образа")
    Column(modifier.clip(RoundedCornerShape(12.dp))
        .background(if (selected) Gold.copy(alpha = .12f) else Color(0xFF182F39))
        .border(if (selected) 2.dp else 1.dp,
            borderColor, RoundedCornerShape(12.dp))
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
        .padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CharacterPortrait(appearance, outfit, Modifier.fillMaxWidth().aspectRatio(if (portrait) 1f else 1.3f)
            .clip(RoundedCornerShape(8.dp)), crop = if (portrait) PortraitCrop.FACE else PortraitCrop.OUTFIT)
        Text(if (selected) "✓ $label" else label, color = if (selected) Gold else Paper,
            fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 9.dp))
    }
}
