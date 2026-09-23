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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.proyectopasantia.data.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

@Composable
fun ContactsScreen(
    onBackClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<Contact?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
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
            .collection("contacts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        "Permisos insuficientes en Firebase Firestore. Configura las reglas de seguridad en la consola de Firebase."
                    } else {
                        "Error al cargar contactos: ${error.localizedMessage}"
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    errorMessage = null
                    val fetchedContacts = snapshot.documents.map { document ->
                        Contact(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            phone = document.getString("phone") ?: "",
                            email = document.getString("email") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }
                    contacts = fetchedContacts
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
                text = "Mis contactos",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(onClick = onBackClick) {
                Text("Volver")
            }
        }

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
                errorMessage = null
                dialogErrorMessage = null
                showDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nuevo contacto")
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

        if (contacts.isEmpty() && errorMessage == null) {
            Text(
                text = "Todavía no tienes contactos.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (contacts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = contacts,
                    key = { contact -> contact.id }
                ) { contact ->
                    ContactCard(
                        contact = contact,
                        onEdit = {
                            editingContact = contact
                            name = contact.name
                            phone = contact.phone
                            email = contact.email
                            errorMessage = null
                            dialogErrorMessage = null
                            showDialog = true
                        },
                        onDelete = {
                            contacts = contacts.filter { it.id != contact.id }
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
                if (!isSaving) showDialog = false
            },
            title = {
                Text(if (editingContact == null) "Nuevo contacto" else "Editar contacto")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            dialogErrorMessage = null
                        },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
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
                            name.isBlank() -> {
                                dialogErrorMessage = "El nombre es obligatorio"
                            }
                            phone.isBlank() && email.isBlank() -> {
                                dialogErrorMessage = "Ingresa un teléfono o correo electrónico"
                            }
                            else -> {
                                dialogErrorMessage = null
                                isSaving = true
                                val currentTimestamp = System.currentTimeMillis()
                                val data = hashMapOf(
                                    "name" to name.trim(),
                                    "phone" to phone.trim(),
                                    "email" to email.trim(),
                                    "timestamp" to currentTimestamp
                                )

                                if (editingContact == null) {
                                    val docRef = firestore
                                        .collection("users")
                                        .document(user.uid)
                                        .collection("contacts")
                                        .document()

                                    val newContact = Contact(
                                        id = docRef.id,
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        timestamp = currentTimestamp
                                    )

                                    contacts = listOf(newContact) + contacts.filter { it.id != newContact.id }

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
                                                e.localizedMessage ?: "Error al guardar contacto"
                                            }
                                            dialogErrorMessage = msg
                                        }
                                } else {
                                    val updatedContact = editingContact!!.copy(
                                        name = name.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        timestamp = currentTimestamp
                                    )

                                    contacts = contacts.map { if (it.id == updatedContact.id) updatedContact else it }

                                    firestore
                                        .collection("users")
                                        .document(user.uid)
                                        .collection("contacts")
                                        .document(updatedContact.id)
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
                                                e.localizedMessage ?: "Error al actualizar contacto"
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

            if (contact.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Teléfono: ${contact.phone}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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
