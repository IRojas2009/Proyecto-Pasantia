package com.example.proyectopasantia.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onBackClick) {
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
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis notas",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onBackClick) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Organiza tus notas por categorías.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva nota")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
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
                        onClick = { retryTrigger++ }
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (notes.isEmpty() && errorMessage == null) {
            Text(
                text = "Todavía no tienes notas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (notes.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
            title = {
                Text(if (editingNote == null) "Nueva nota" else "Editar nota")
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
                        singleLine = true,
                        enabled = !isSaving,
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
                        enabled = !isSaving,
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
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (dialogErrorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = dialogErrorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSaving,
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
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar")
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleLarge
            )

            if (note.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Categoría: ${note.category}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Text("Eliminar")
                }
            }
        }
    }
}
