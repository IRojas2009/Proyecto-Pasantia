package com.example.proyectopasantia.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectopasantia.data.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ContactsScreen() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<Contact?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

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
        }
        return
    }

    LaunchedEffect(user.uid) {
        firestore
            .collection("users")
            .document(user.uid)
            .collection("contacts")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    contacts = snapshot.documents.map { document ->
                        Contact(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            phone = document.getString("phone") ?: "",
                            email = document.getString("email") ?: ""
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
            text = "Mis contactos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Administra tus contactos en un solo lugar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                editingContact = null
                name = ""
                phone = ""
                email = ""
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo contacto")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (contacts.isEmpty()) {
            Text(
                text = "Todavía no tienes contactos.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contacts) { contact ->

                    ContactCard(
                        contact = contact,

                        onEdit = {
                            editingContact = contact
                            name = contact.name
                            phone = contact.phone
                            email = contact.email
                            showDialog = true
                        },

                        onDelete = {
                            firestore
                                .collection("users")
                                .document(user.uid)
                                .collection("contacts")
                                .document(contact.id)
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
                    if (editingContact == null) {
                        "Nuevo contacto"
                    } else {
                        "Editar contacto"
                    }
                )
            },

            text = {
                Column {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = {
                            Text("Nombre")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = {
                            Text("Teléfono")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = {
                            Text("Correo electrónico")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        if (
                            name.isNotBlank() &&
                            phone.isNotBlank()
                        ) {

                            val data = hashMapOf(
                                "name" to name.trim(),
                                "phone" to phone.trim(),
                                "email" to email.trim()
                            )

                            if (editingContact == null) {

                                firestore
                                    .collection("users")
                                    .document(user.uid)
                                    .collection("contacts")
                                    .add(data)

                            } else {

                                firestore
                                    .collection("users")
                                    .document(user.uid)
                                    .collection("contacts")
                                    .document(editingContact!!.id)
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
fun ContactCard(
    contact: Contact,
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
                text = contact.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Teléfono: ${contact.phone}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (contact.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Correo: ${contact.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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