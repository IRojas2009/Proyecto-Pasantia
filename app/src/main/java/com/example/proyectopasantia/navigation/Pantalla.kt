package com.example.proyectopasantia.navigation

sealed class Pantalla(val route: String) {
    object Login : Pantalla("login")
    object Registro : Pantalla("register")
    object Inicio : Pantalla("home")
    object Notas : Pantalla("notes")
    object Contactos : Pantalla("contacts")
}
