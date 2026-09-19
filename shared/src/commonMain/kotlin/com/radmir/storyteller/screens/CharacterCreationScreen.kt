package com.radmir.storyteller.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.radmir.storyteller.models.PlayerAppearance
import com.radmir.storyteller.models.PlayerCharacter
import com.radmir.storyteller.models.PlayerOutfit

@Composable
fun CharacterCreationScreen(
    onCharacterCreated: (PlayerCharacter) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var appearance by remember { mutableStateOf(PlayerAppearance.DARK_HAIR) }
    var outfit by remember { mutableStateOf(PlayerOutfit.JACKET) }
    val normalizedName = name.trim()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Назад") }
            Text("Создание персонажа", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(20.dp))
        CharacterPreview(appearance, outfit)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 24) name = it },
            label = { Text("Имя персонажа") },
            supportingText = { Text("От 1 до 24 символов") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        ChoiceSection(
            title = "Внешность",
            options = listOf(
                PlayerAppearance.LIGHT_HAIR to "Светлые волосы",
                PlayerAppearance.DARK_HAIR to "Тёмные волосы",
                PlayerAppearance.RED_HAIR to "Рыжие волосы"
            ),
            selected = appearance,
            onSelected = { appearance = it }
        )
        Spacer(Modifier.height(20.dp))
        ChoiceSection(
            title = "Одежда",
            options = listOf(
                PlayerOutfit.JACKET to "Куртка",
                PlayerOutfit.SWEATER to "Свитер",
                PlayerOutfit.COAT to "Плащ"
            ),
            selected = outfit,
            onSelected = { outfit = it }
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onCharacterCreated(PlayerCharacter(normalizedName, appearance, outfit)) },
            enabled = normalizedName.isNotEmpty(),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать историю  →")
        }
    }
}

@Composable
private fun <T> ChoiceSection(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelected: (T) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        options.forEach { (value, label) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CharacterPreview(appearance: PlayerAppearance, outfit: PlayerOutfit) {
    val hairColor = when (appearance) {
        PlayerAppearance.LIGHT_HAIR -> Color(0xFFE7C978)
        PlayerAppearance.DARK_HAIR -> Color(0xFF302822)
        PlayerAppearance.RED_HAIR -> Color(0xFF9F4A2F)
    }
    val outfitColor = when (outfit) {
        PlayerOutfit.JACKET -> Color(0xFF315A72)
        PlayerOutfit.SWEATER -> Color(0xFF7C4D68)
        PlayerOutfit.COAT -> Color(0xFF445447)
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.size(width = 220.dp, height = 250.dp)
    ) {
        Canvas(Modifier.fillMaxSize().padding(18.dp)) {
            val cx = size.width / 2f
            val headRadius = size.minDimension * 0.18f
            val headCenter = Offset(cx, size.height * 0.30f)
            drawRoundRect(
                color = outfitColor,
                topLeft = Offset(size.width * 0.22f, size.height * 0.50f),
                size = Size(size.width * 0.56f, size.height * 0.46f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.12f)
            )
            if (outfit == PlayerOutfit.COAT) {
                drawLine(Color(0xFFE3BB79), Offset(cx, size.height * 0.55f),
                    Offset(cx, size.height * 0.94f), strokeWidth = 4f)
            }
            drawCircle(Color(0xFFE7BFA3), headRadius, headCenter)
            drawArc(
                color = hairColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius),
                size = Size(headRadius * 2f, headRadius * 2f)
            )
            drawCircle(Color(0xFF263238), 3.5f,
                Offset(headCenter.x - headRadius * 0.35f, headCenter.y + headRadius * 0.10f))
            drawCircle(Color(0xFF263238), 3.5f,
                Offset(headCenter.x + headRadius * 0.35f, headCenter.y + headRadius * 0.10f))
        }
    }
}
