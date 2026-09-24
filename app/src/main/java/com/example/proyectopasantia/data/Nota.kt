package com.example.proyectopasantia.data

data class Nota(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
