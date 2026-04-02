package com.example.apprecorder.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apprecorder.data.model.Note
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Expandable card that shows a single [Note].
 *
 * - **Header (always visible):** recording time formatted as HH:mm.
 * - **Body (expanded on tap):** full transcribed text with a smooth animation.
 */
@Composable
fun NoteCard(note: Note) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: time only
            Text(
                text = formatNoteTime(note.createdAt),
                style = MaterialTheme.typography.titleMedium
            )

            // Body: animated expand/collapse
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = note.text,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Parses an ISO-8601 timestamp returned by Supabase (e.g. "2024-01-15T10:30:00.123456+00:00")
 * and formats it as "HH:mm" in the local time zone.
 * Returns the raw string unchanged if parsing fails.
 */
private fun formatNoteTime(createdAt: String): String {
    return try {
        val odt = OffsetDateTime.parse(createdAt)
        odt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        createdAt
    }
}
