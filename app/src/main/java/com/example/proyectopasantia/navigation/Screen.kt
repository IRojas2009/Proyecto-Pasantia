package com.example.proyectopasantia.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Notes : Screen("notes")
    object Contacts : Screen("contacts")
}
