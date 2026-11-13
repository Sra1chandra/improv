package app.sandy.improv.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private val Models = listOf("gpt-4o-mini", "gpt-4o")
private val PencilBox = RoundedCornerShape(2.dp)

@Composable
fun SettingsScreen(
    apiKey: String,
    model: String,
    onApiKey: (String) -> Unit,
    onModel: (String) -> Unit,
    onBack: () -> Unit,
) {
    val shownModel = model.ifBlank { "gpt-4o-mini" }
    val options = if (shownModel in Models) Models else listOf(shownModel) + Models

    Box(
        Modifier
            .fillMaxSize()
            .background(Ink.desk)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
    ) {
        PaperSheet(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 12.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink.felt,
                        )
                    }
                    Text("Settings", style = MaterialTheme.typography.titleLarge)
                }
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("API key", color = Ink.pencil, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = onApiKey,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = PencilBox,
                        colors = paperFieldColors(),
                    )
                    Text("Model", color = Ink.pencil, style = MaterialTheme.typography.bodySmall)
                    PaperSelect(
                        value = shownModel,
                        options = options,
                        onPick = onModel,
                    )
                    Text(
                        "Key stays on this phone.",
                        color = Ink.pencil,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun PaperSelect(
    value: String,
    options: List<String>,
    onPick: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val fieldWidth = maxWidth
        Row(
            Modifier
                .fillMaxWidth()
                .border(1.dp, Ink.felt, PencilBox)
                .background(Ink.paperFace, PencilBox)
                .clickable(role = Role.DropdownList) { open = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, color = Ink.you, style = MaterialTheme.typography.bodyLarge)
            Text("▾", color = Ink.felt, style = MaterialTheme.typography.bodyLarge)
        }
        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            modifier = Modifier.requiredWidth(fieldWidth),
            shape = PencilBox,
            containerColor = Ink.paperFace,
            tonalElevation = 0.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, Ink.felt),
        ) {
            options.forEach { option ->
                val chosen = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = Ink.felt,
                            fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    trailingIcon = {
                        if (chosen) Text("✓", color = Ink.you)
                    },
                    onClick = {
                        onPick(option)
                        open = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Ink.felt,
                        trailingIconColor = Ink.you,
                    ),
                    modifier = if (chosen) Modifier.background(Ink.highlight) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun paperFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink.felt,
    unfocusedBorderColor = Ink.felt,
    focusedContainerColor = Ink.paperFace,
    unfocusedContainerColor = Ink.paperFace,
    cursorColor = Ink.you,
    focusedTextColor = Ink.you,
    unfocusedTextColor = Ink.you,
)
