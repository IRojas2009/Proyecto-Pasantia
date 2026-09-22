package com.example.proyectopasantia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register")
                },
                onLoginClick = {
                    navController.navigate("home")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNotesClick = {
                    navController.navigate("notes")
                },
                onContactsClick = {
                    navController.navigate("contacts")
                }
            )
        }

        composable("notes") {
            NotesScreen()
        }

        composable("contacts") {
            ContactsScreen()
        }
    }
}

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Librería de notas y contactos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Organiza tus notas y contactos en un solo lugar."
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear cuenta")
        }
    }
}

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text("Correo electrónico")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text("Contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = {
                Text("Confirmar contraseña")
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Firebase irá aquí.
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarme")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al inicio de sesión")
        }
    }
}


@Composable
fun HomeScreen(
    onNotesClick: () -> Unit,
    onContactsClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNotesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Notas")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onContactsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Contactos")
        }
    }
}

@Composable
fun NotesScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Notas",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aquí aparecerán tus notas."
        )
    }
}

@Composable
fun ContactsScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Contactos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aquí aparecerán tus contactos."
        )
    }
}