package com.example.proyectopasantia.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyectopasantia.data.Contacto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

data class Pais(
    val name: String,
    val code: String,
    val length: Int
)

val paises = listOf(
    Pais("Costa Rica", "+506", 8),
    Pais("Estados Unidos", "+1", 10),
    Pais("México", "+52", 10),
    Pais("España", "+34", 9),
    Pais("Colombia", "+57", 10),
    Pais("Personalizado", "+", 0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaContactos(
    onBackClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var contacts by remember { mutableStateOf<List<Contacto>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<Contacto?>(null) }

    var name by remember { mutableStateOf("") }
    var phoneDigits by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(paises[0]) }
    var customCountryCode by remember { mutableStateOf("+") }
    var countryExpanded by remember { mutableStateOf(false) }

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
                        Contacto(
                            id = document.id,
                            name = document.getString("name") ?: "",
                            phone = document.getString("phone") ?: "",
                            email = document.getString("email") ?: "",
                            timestamp = document.getLong("timestamp") ?: 0L,
                            isFavorite = document.getBoolean("isFavorite") ?: false
                        )
                    }.sortedWith(compareByDescending<Contacto> { it.isFavorite }.thenByDescending { it.timestamp })
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
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mis contactos",
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
            text = "Administra tus contactos en un solo lugar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                editingContact = null
                name = ""
                phoneDigits = ""
                email = ""
                isFavorite = false
                selectedCountry = paises[0]
                customCountryCode = "+"
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
                text = "Nuevo contacto",
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

        if (contacts.isEmpty() && errorMessage == null) {
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
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Todavía no tienes contactos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (contacts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = contacts,
                    key = { contact -> contact.id }
                ) { contact ->
                    TarjetaContacto(
                        contacto = contact,
                        onEdit = {
                            editingContact = contact
                            name = contact.name
                            email = contact.email
                            isFavorite = contact.isFavorite

                            val matchedCountry = paises.find { it.code != "+" && contact.phone.startsWith(it.code) }
                            if (matchedCountry != null) {
                                selectedCountry = matchedCountry
                                phoneDigits = contact.phone.removePrefix(matchedCountry.code).trim().filter { it.isDigit() }
                            } else if (contact.phone.startsWith("+")) {
                                selectedCountry = paises.last() // Personalizado
                                val parts = contact.phone.split(" ", limit = 2)
                                if (parts.isNotEmpty()) {
                                    customCountryCode = parts[0]
                                    phoneDigits = if (parts.size > 1) parts[1].filter { it.isDigit() } else ""
                                }
                            } else {
                                selectedCountry = paises[0]
                                phoneDigits = contact.phone.filter { it.isDigit() }
                            }

                            errorMessage = null
                            dialogErrorMessage = null
                            showDialog = true
                        },
                        onToggleFavorite = {
                            val newFavorite = !contact.isFavorite
                            contacts = contacts.map { if (it.id == contact.id) it.copy(isFavorite = newFavorite) else it }
                                .sortedWith(compareByDescending<Contacto> { it.isFavorite }.thenByDescending { it.timestamp })
                            
                            val data = hashMapOf(
                                "name" to contact.name,
                                "phone" to contact.phone,
                                "email" to contact.email,
                                "timestamp" to contact.timestamp,
                                "isFavorite" to newFavorite
                            )
                            firestore
                                .collection("users")
                                .document(user.uid)
                                .collection("contacts")
                                .document(contact.id)
                                .set(data)
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
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = if (editingContact == null) "Nuevo contacto" else "Editar contacto",
                    fontWeight = FontWeight.Bold
                )
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
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Country Dropdown
                    ExposedDropdownMenuBox(
                        expanded = countryExpanded,
                        onExpandedChange = { countryExpanded = !countryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (selectedCountry.code == "+") "Personalizado" else "${selectedCountry.name} (${selectedCountry.code})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("País / Código") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = countryExpanded,
                            onDismissRequest = { countryExpanded = false }
                        ) {
                            paises.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text(if (country.code == "+") "Personalizado (+...)" else "${country.name} (${country.code})") },
                                    onClick = {
                                        selectedCountry = country
                                        countryExpanded = false
                                        dialogErrorMessage = null
                                    }
                                )
                            }
                        }
                    }

                    if (selectedCountry.code == "+") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customCountryCode,
                            onValueChange = {
                                customCountryCode = if (it.startsWith("+")) it else "+$it"
                                dialogErrorMessage = null
                            },
                            label = { Text("Código de país personalizado (ej. +54)") },
                            singleLine = true,
                            enabled = !isSaving,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneDigits,
                        onValueChange = {
                            phoneDigits = it.filter { char -> char.isDigit() }
                            dialogErrorMessage = null
                        },
                        label = { Text(if (selectedCountry.code == "+") "Número de teléfono" else "Teléfono (${selectedCountry.length} dígitos)") },
                        leadingIcon = {
                            Text(
                                text = if (selectedCountry.code == "+") (if (customCountryCode.isBlank()) "+" else customCountryCode) else selectedCountry.code,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                            )
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
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
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        singleLine = true,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isFavorite = !isFavorite }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Marcar como favorito",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

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
                        val cleanDigits = phoneDigits.filter { it.isDigit() }
                        when {
                            name.isBlank() -> {
                                dialogErrorMessage = "El nombre es obligatorio"
                            }
                            phoneDigits.isNotBlank() && selectedCountry.code != "+" && cleanDigits.length != selectedCountry.length -> {
                                dialogErrorMessage = "Para ${selectedCountry.name} (${selectedCountry.code}), el número debe tener exactamente ${selectedCountry.length} dígitos (ingresaste ${cleanDigits.length})."
                            }
                            phoneDigits.isNotBlank() && selectedCountry.code == "+" && (customCountryCode.length < 2 || cleanDigits.length < 4) -> {
                                dialogErrorMessage = "Ingresa un código de país válido (ej. +54) y un número de teléfono."
                            }
                            phoneDigits.isBlank() && email.isBlank() -> {
                                dialogErrorMessage = "Ingresa un teléfono o correo electrónico"
                            }
                            else -> {
                                dialogErrorMessage = null
                                isSaving = true
                                val finalPrefix = if (selectedCountry.code == "+") {
                                    if (customCountryCode.startsWith("+")) customCountryCode else "+$customCountryCode"
                                } else {
                                    selectedCountry.code
                                }
                                val finalPhone = if (phoneDigits.isBlank()) "" else "$finalPrefix $cleanDigits"
                                val currentTimestamp = System.currentTimeMillis()
                                val data = hashMapOf(
                                    "name" to name.trim(),
                                    "phone" to finalPhone,
                                    "email" to email.trim(),
                                    "timestamp" to currentTimestamp,
                                    "isFavorite" to isFavorite
                                )

                                if (editingContact == null) {
                                    val docRef = firestore
                                        .collection("users")
                                        .document(user.uid)
                                        .collection("contacts")
                                        .document()

                                    val newContact = Contacto(
                                        id = docRef.id,
                                        name = name.trim(),
                                        phone = finalPhone,
                                        email = email.trim(),
                                        timestamp = currentTimestamp,
                                        isFavorite = isFavorite
                                    )

                                    contacts = (listOf(newContact) + contacts.filter { it.id != newContact.id })
                                        .sortedWith(compareByDescending<Contacto> { it.isFavorite }.thenByDescending { it.timestamp })

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
                                        phone = finalPhone,
                                        email = email.trim(),
                                        timestamp = currentTimestamp,
                                        isFavorite = isFavorite
                                    )

                                    contacts = contacts.map { if (it.id == updatedContact.id) updatedContact else it }
                                        .sortedWith(compareByDescending<Contacto> { it.isFavorite }.thenByDescending { it.timestamp })

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
fun TarjetaContacto(
    contacto: Contacto,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contacto.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (contacto.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (contacto.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (contacto.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = contacto.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contacto.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = "Enviar SMS",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacto.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Llamar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (contacto.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = contacto.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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
