package com.example.proyectopasantia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectopasantia.ui.screens.PantallaContactos
import com.example.proyectopasantia.ui.screens.PantallaInicio
import com.example.proyectopasantia.ui.screens.PantallaLogin
import com.example.proyectopasantia.ui.screens.PantallaNotas
import com.example.proyectopasantia.ui.screens.PantallaRegistro
import com.example.proyectopasantia.viewmodel.AutenticacionViewModel

@Composable
fun NavegacionApp(
    navController: NavHostController = rememberNavController(),
    autenticacionViewModel: AutenticacionViewModel = viewModel()
) {
    val loginState by autenticacionViewModel.loginState.collectAsState()
    val registerState by autenticacionViewModel.registerState.collectAsState()

    val startDestination = if (autenticacionViewModel.isUserLoggedIn) {
        Pantalla.Inicio.route
    } else {
        Pantalla.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Pantalla.Login.route) {
            PantallaLogin(
                state = loginState,
                onEmailChanged = autenticacionViewModel::onLoginEmailChanged,
                onPasswordChanged = autenticacionViewModel::onLoginPasswordChanged,
                onLoginClick = {
                    autenticacionViewModel.login {
                        navController.navigate(Pantalla.Inicio.route) {
                            popUpTo(Pantalla.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onRegisterClick = {
                    autenticacionViewModel.clearLoginError()
                    navController.navigate(Pantalla.Registro.route)
                },
                onForgotPasswordClick = {
                    autenticacionViewModel.sendPasswordReset()
                }
            )
        }

        composable(Pantalla.Registro.route) {
            PantallaRegistro(
                state = registerState,
                onEmailChanged = autenticacionViewModel::onRegisterEmailChanged,
                onPasswordChanged = autenticacionViewModel::onRegisterPasswordChanged,
                onConfirmPasswordChanged = autenticacionViewModel::onRegisterConfirmPasswordChanged,
                onRegisterClick = {
                    autenticacionViewModel.register {
                        navController.popBackStack()
                    }
                },
                onBackToLogin = {
                    autenticacionViewModel.clearRegisterError()
                    navController.popBackStack()
                }
            )
        }

        composable(Pantalla.Inicio.route) {
            PantallaInicio(
                onNotesClick = {
                    navController.navigate(Pantalla.Notas.route)
                },
                onContactsClick = {
                    navController.navigate(Pantalla.Contactos.route)
                },
                onLogoutClick = {
                    autenticacionViewModel.logout {
                        navController.navigate(Pantalla.Login.route) {
                            popUpTo(Pantalla.Inicio.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Pantalla.Notas.route) {
            PantallaNotas(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Pantalla.Contactos.route) {
            PantallaContactos(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
