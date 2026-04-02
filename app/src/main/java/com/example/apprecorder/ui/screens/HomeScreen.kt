package com.example.apprecorder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apprecorder.ui.NoteViewModel
import com.example.apprecorder.ui.screens.components.NoteCard

@Composable
fun HomeScreen(viewModel: NoteViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startRecording()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Großer runder Button ───────────────────────────────────────────
        val buttonColor = if (uiState.isRecording)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary

        val iconColor = if (uiState.isRecording)
            MaterialTheme.colorScheme.onError
        else
            MaterialTheme.colorScheme.onPrimary

        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(buttonColor)
                .clickable {
                    if (uiState.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        val permission = Manifest.permission.RECORD_AUDIO
                        if (ContextCompat.checkSelfPermission(context, permission) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.startRecording()
                        } else {
                            permissionLauncher.launch(permission)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (uiState.isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (uiState.isRecording) "Aufnahme stoppen" else "Aufnahme starten",
                tint = iconColor,
                modifier = Modifier.size(104.dp)
            )
        }

        // ── Live-Transkription ─────────────────────────────────────────────
        if (uiState.partialTranscription.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.partialTranscription,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // ── Fehlermeldung ──────────────────────────────────────────────────
        val error = uiState.error
        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Notizenliste ───────────────────────────────────────────────────
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            var expandedNoteId by rememberSaveable { mutableStateOf<Long?>(null) }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(
                    items = uiState.notes,
                    key = { note -> note.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        expanded = expandedNoteId == note.id,
                        onToggle = {
                            expandedNoteId = if (expandedNoteId == note.id) null else note.id
                        }
                    )
                }
            }
        }
    }
}
