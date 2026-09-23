package com.example.proyectopasantia.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectopasantia.ui.screens.ContactsScreen
import com.example.proyectopasantia.ui.screens.HomeScreen
import com.example.proyectopasantia.ui.screens.LoginScreen
import com.example.proyectopasantia.ui.screens.NotesScreen
import com.example.proyectopasantia.ui.screens.RegisterScreen
import com.example.proyectopasantia.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val loginState by authViewModel.loginState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()

    val startDestination = if (authViewModel.isUserLoggedIn) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                state = loginState,
                onEmailChanged = authViewModel::onLoginEmailChanged,
                onPasswordChanged = authViewModel::onLoginPasswordChanged,
                onLoginClick = {
                    authViewModel.login {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onRegisterClick = {
                    authViewModel.clearLoginError()
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                state = registerState,
                onEmailChanged = authViewModel::onRegisterEmailChanged,
                onPasswordChanged = authViewModel::onRegisterPasswordChanged,
                onConfirmPasswordChanged = authViewModel::onRegisterConfirmPasswordChanged,
                onRegisterClick = {
                    authViewModel.register {
                        navController.popBackStack()
                    }
                },
                onBackToLogin = {
                    authViewModel.clearRegisterError()
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNotesClick = {
                    navController.navigate(Screen.Notes.route)
                },
                onContactsClick = {
                    navController.navigate(Screen.Contacts.route)
                },
                onLogoutClick = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
