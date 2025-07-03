package com.example.notesfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Add
import androidx.compose.material3.icons.filled.Delete
import androidx.compose.material3.icons.filled.Edit
import androidx.compose.material3.icons.filled.ArrowBack
import androidx.compose.material3.icons.filled.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

// #region Data model

/**
 * A simple data class representing a Note.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String
)

/**
 * Provide a sample list for preview or first-run.
 */
fun sampleNotes() = listOf(
    Note(title = "Meeting Ideas", content = "Discuss project roadmap and deadlines."),
    Note(title = "Shopping List", content = "Milk, Eggs, Bread, Butter")
)

// #endregion

/**
 * Main activity sets Compose content and theme.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {
                NotesApp()
            }
        }
    }
}

// #region Theme

@Composable
fun NotesAppTheme(content: @Composable () -> Unit) {
    // Colors as per request
    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF1976D2),
        secondary = Color(0xFF424242),
        tertiary = Color(0xFFFFC107),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF8F9FA),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = Color.Black,
        onSurface = Color.Black,
    )
    MaterialTheme(
        colorScheme = lightColorScheme,
        typography = Typography(),
        content = content
    )
}

// #endregion

// #region Main App Root

/**
 * Root composable managing navigation and note state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp() {
    var notes by remember { mutableStateOf(sampleNotes().toMutableList()) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Main Nav controller
    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            showEditor -> NoteEditorScreen(
                initialNote = editingNote,
                onSave = { note ->
                    if (editingNote == null) {
                        notes = (notes + note).toMutableList()
                    } else {
                        notes = notes.map { if (it.id == note.id) note else it }.toMutableList()
                    }
                    showEditor = false
                    editingNote = null
                },
                onCancel = {
                    showEditor = false
                    editingNote = null
                }
            )
            selectedNote != null -> NoteDetailScreen(
                note = selectedNote!!,
                onBack = { selectedNote = null },
                onEdit = {
                    editingNote = selectedNote
                    showEditor = true
                },
                onDelete = { showDeleteDialog = true }
            )
            else -> NotesListScreen(
                notes = notes,
                onNoteSelected = { selectedNote = it },
                onAddNote = {
                    editingNote = null
                    showEditor = true
                }
            )
        }
        if (showDeleteDialog && selectedNote != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        notes = notes.filterNot { it.id == selectedNote!!.id }.toMutableList()
                        selectedNote = null
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Note?") },
                text = { Text("Are you sure you want to delete this note? This action cannot be undone.") }
            )
        }
    }
}

// #endregion

// #region Screens

/**
 * Modern main list screen for notes, with FAB for new note.
 */
@Composable
fun NotesListScreen(
    notes: List<Note>,
    onNoteSelected: (Note) -> Unit,
    onAddNote: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddNote() },
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add note")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                "Your Notes",
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                modifier = Modifier.padding(24.dp, 24.dp, 24.dp, 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            if (notes.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes yet. Tap + to create your first note!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onNoteSelected(note) })
                    }
                }
            }
        }
    }
}

/**
 * Card for a single note in the list.
 */
@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() },
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(note.title, fontWeight = FontWeight.Medium, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content.take(100),
                    style = TextStyle(color = Color.DarkGray),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Detail screen for viewing a single note.
 */
@Composable
fun NoteDetailScreen(
    note: Note,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                note.content,
                style = TextStyle(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Editor for creating/updating a note.
 */
@Composable
fun NoteEditorScreen(
    initialNote: Note?,
    onSave: (Note) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initialNote == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isBlank()) {
                            showError = true
                        } else {
                            val note = initialNote?.copy(title = title, content = content)
                                ?: Note(title = title, content = content)
                            onSave(note)
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    showError = false
                },
                label = { Text("Title") },
                isError = showError,
                modifier = Modifier.fillMaxWidth()
            )
            if (showError) {
                Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                maxLines = 10,
                singleLine = false
            )
        }
    }
}

// #endregion
