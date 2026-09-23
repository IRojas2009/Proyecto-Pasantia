package com.example.proyectopasantia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectopasantia.data.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

@Composable
fun NotesScreen(
    onBackClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dialogErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }

    if (user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay una sesión activa.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Volver")
            }
        }
        return
    }

    DisposableEffect(user.uid, retryTrigger) {
        errorMessage = null
        val listener = firestore
            .collection("users")
            .document(user.uid)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        "Permisos insuficientes en Firebase Firestore. Configura las reglas de seguridad en la consola de Firebase."
                    } else {
                        "Error al cargar notas: ${error.localizedMessage}"
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    errorMessage = null
                    val fetchedNotes = snapshot.documents.map { document ->
                        Note(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            content = document.getString("content") ?: "",
                            category = document.getString("category") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }
                    notes = fetchedNotes
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen-centered title header with back button on the left
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mis notas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Organiza tus notas por categorías",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                editingNote = null
                title = ""
                content = ""
                category = ""
                errorMessage = null
                dialogErrorMessage = null
                showDialog = true
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.padding(horizontal = 6.dp))
            Text(
                text = "Nueva nota",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { retryTrigger++ },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (notes.isEmpty() && errorMessage == null) {
            Spacer(modifier = Modifier.height(48.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Todavía no tienes notas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (notes.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = notes,
                    key = { note -> note.id }
                ) { note ->
                    NoteCard(
                        note = note,
                        onEdit = {
                            editingNote = note
                            title = note.title
                            content = note.content
                            category = note.category
                            errorMessage = null
                            dialogErrorMessage = null
                            showDialog = true
                        },
                        onDelete = {
                            notes = notes.filter { it.id != note.id }
                            firestore
                                .collection("users")
                                .document(user.uid)
                                .collection("notes")
                                .document(note.id)
                                .delete()
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSaving) showDialog = false
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = if (editingNote == null) "Nueva nota" else "Editar nota",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Título") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Title, contentDescription = null)
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Contenido") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Note, contentDescription = null)
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Categoría") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null)
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (dialogErrorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = dialogErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        when {
                            title.isBlank() -> {
                                dialogErrorMessage = "El título es obligatorio"
                            }
                            content.isBlank() -> {
                                dialogErrorMessage = "El contenido es obligatorio"
                            }
                            else -> {
                                dialogErrorMessage = null
                                isSaving = true
                                val currentTimestamp = System.currentTimeMillis()
                                val data = hashMapOf(
                                    "title" to title.trim(),
                                    "content" to content.trim(),
                                    "category" to category.trim(),
                                    "timestamp" to currentTimestamp
                                )

                                if (editingNote == null) {
                                    val docRef = firestore
                                        .collection("users")
                                        .document(user.uid)
                                        .collection("notes")
                                        .document()

                                    val newNote = Note(
                                        id = docRef.id,
                                        title = title.trim(),
                                        content = content.trim(),
                                        category = category.trim(),
                                        timestamp = currentTimestamp
                                    )

                                    notes = listOf(newNote) + notes.filter { it.id != newNote.id }

                                    docRef.set(data)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            showDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            isSaving = false
                                            val msg = if ((e as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                                "Permisos insuficientes en Firestore. Revisa las reglas de seguridad."
                                            } else {
                                                e.localizedMessage ?: "Error al guardar nota"
                                            }
                                            dialogErrorMessage = msg
                                        }
                                } else {
                                    val updatedNote = editingNote!!.copy(
                                        title = title.trim(),
                                        content = content.trim(),
                                        category = category.trim(),
                                        timestamp = currentTimestamp
                                    )

                                    notes = notes.map { if (it.id == updatedNote.id) updatedNote else it }

                                    firestore
                                        .collection("users")
                                        .document(user.uid)
                                        .collection("notes")
                                        .document(updatedNote.id)
                                        .set(data)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            showDialog = false
                                        }
                                        .addOnFailureListener { e ->
                                            isSaving = false
                                            val msg = if ((e as? FirebaseFirestoreException)?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                                "Permisos insuficientes en Firestore. Revisa las reglas de seguridad."
                                            } else {
                                                e.localizedMessage ?: "Error al actualizar nota"
                                            }
                                            dialogErrorMessage = msg
                                        }
                                }
                            }
                        }
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSaving,
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (note.category.isNotBlank()) {
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text("Editar", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text("Eliminar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
