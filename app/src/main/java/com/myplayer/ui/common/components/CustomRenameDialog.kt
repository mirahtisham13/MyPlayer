package com.myplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reusable dialog for renaming files/folders or naming a new folder.
 *
 * @param initialName  Pre-filled text (use "" for new-folder flow).
 * @param title        Dialog heading.
 * @param confirmLabel Label for the confirm button (default "Rename").
 * @param placeholder  TextField placeholder text.
 * @param subtitle     Optional secondary description line.
 * @param onConfirm    Called with the trimmed, non-blank value.
 * @param onDismiss    Called when the dialog is dismissed without confirmation.
 */
@Composable
fun CustomRenameDialog(
    initialName: String,
    title: String = "Rename",
    confirmLabel: String = "Rename",
    placeholder: String = "New name",
    subtitle: String? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val textFieldState = androidx.compose.foundation.text.input.rememberTextFieldState(initialText = initialName)
    val isNewFolder = confirmLabel != "Rename"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isNewFolder) Icons.Rounded.CreateNewFolder
                        else Icons.Filled.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle ?: "Enter a new name below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    state = textFieldState,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(placeholder) },
                    lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    val isValid = textFieldState.text.isNotBlank() &&
                            (isNewFolder || textFieldState.text.toString().trim() != initialName)

                    Button(
                        onClick = { if (isValid) onConfirm(textFieldState.text.toString().trim()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(confirmLabel, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
