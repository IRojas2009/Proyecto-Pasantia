package com.example.proyectopasantia.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    fun onLoginEmailChanged(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _loginState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Completa todos los campos") }
            return
        }

        _loginState.update { it.copy(isLoading = true, errorMessage = null) }

        auth.signInWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginState.update { LoginUiState() }
                    onSuccess()
                } else {
                    val message = task.exception?.localizedMessage
                        ?: "Correo o contraseña incorrectos"
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message
                        )
                    }
                }
            }
    }

    fun onRegisterEmailChanged(email: String) {
        _registerState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onRegisterPasswordChanged(password: String) {
        _registerState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRegisterConfirmPasswordChanged(confirmPassword: String) {
        _registerState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun register(onSuccess: () -> Unit) {
        val state = _registerState.value
        when {
            state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank() -> {
                _registerState.update { it.copy(errorMessage = "Completa todos los campos") }
            }
            state.password != state.confirmPassword -> {
                _registerState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            }
            state.password.length < 6 -> {
                _registerState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            }
            else -> {
                _registerState.update { it.copy(isLoading = true, errorMessage = null) }

                auth.createUserWithEmailAndPassword(state.email.trim(), state.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _registerState.update { RegisterUiState() }
                            auth.signOut()
                            onSuccess()
                        } else {
                            val message = task.exception?.localizedMessage
                                ?: "No se pudo crear la cuenta"
                            _registerState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = message
                                )
                            }
                        }
                    }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        _loginState.value = LoginUiState()
        _registerState.value = RegisterUiState()
        onSuccess()
    }

    fun clearLoginError() {
        _loginState.update { it.copy(errorMessage = null) }
    }

    fun clearRegisterError() {
        _registerState.update { it.copy(errorMessage = null) }
    }
}
