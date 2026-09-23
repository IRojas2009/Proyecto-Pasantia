package com.example.proyectopasantia.data

data class Contact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
