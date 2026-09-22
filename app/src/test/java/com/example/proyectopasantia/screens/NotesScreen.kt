package com.example.proyectopasantia.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectopasantia.data.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NotesScreen() {

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val user = auth.currentUser

    var notes by remember {
        mutableStateOf<List<Note>>(emptyList())
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var editingNote by remember {
        mutableStateOf<Note?>(null)
    }

    var title by remember {
        mutableStateOf("")
    }

    var content by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    if (user == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No hay una sesión activa.",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        return
    }

    LaunchedEffect(user.uid) {

        firestore
            .collection("users")
            .document(user.uid)
            .collection("notes")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {

                    notes = snapshot.documents.map { document ->

                        Note(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            content = document.getString("content") ?: "",
                            category = document.getString("category") ?: ""
                        )
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Mis notas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Organiza tus notas por categorías."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = {

                editingNote = null
                title = ""
                content = ""
                category = ""

                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nueva nota")
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (notes.isEmpty()) {

            Text(
                text = "Todavía no tienes notas."
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(notes) { note ->

                    NoteCard(
                        note = note,

                        onEdit = {

                            editingNote = note
                            title = note.title
                            content = note.content
                            category = note.category

                            showDialog = true
                        },

                        onDelete = {

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
                showDialog = false
            },

            title = {
                Text(
                    if (editingNote == null) {
                        "Nueva nota"
                    } else {
                        "Editar nota"
                    }
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text("Título")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                        },
                        label = {
                            Text("Contenido")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                        },
                        label = {
                            Text("Categoría")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (
                            title.isNotBlank() &&
                            content.isNotBlank()
                        ) {

                            val data = hashMapOf(
                                "title" to title.trim(),
                                "content" to content.trim(),
                                "category" to category.trim()
                            )

                            if (editingNote == null) {

                                firestore
                                    .collection("users")
                                    .document(user.uid)
                                    .collection("notes")
                                    .add(data)

                            } else {

                                firestore
                                    .collection("users")
                                    .document(user.uid)
                                    .collection("notes")
                                    .document(editingNote!!.id)
                                    .set(data)
                            }

                            showDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDialog = false
                    }
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

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Categoría: ${note.category}",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = note.content
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                TextButton(
                    onClick = onEdit
                ) {
                    Text("Editar")
                }

                TextButton(
                    onClick = onDelete
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}