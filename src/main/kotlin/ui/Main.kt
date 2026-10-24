package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import backend.model.Prisoner
import backend.model.User
import backend.service.UserService
import ui.auth.loginScreen
import ui.auth.signinScreen
import ui.register.homeScreen
import ui.register.prisonerManagement
import ui.theme.MemoraTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Memora - Atendimento a Presos",
        state = rememberWindowState(placement = WindowPlacement.Maximized)
    ) {
        MemoraTheme { app() }
    }
}

@Composable
fun app() {
    val service = remember { UserService() }
    var usuarioLogado by remember { mutableStateOf<User?>(null) }
    var selectedPrisoner by remember { mutableStateOf<Prisoner?>(null) }
    var wannaSignin by remember { mutableStateOf(false) }

    val logado = usuarioLogado
    val selected = selectedPrisoner
    val signin = wannaSignin

    if (logado == null) {
        if (!signin) {
            loginScreen(service, { usuarioLogado = it }) { wannaSignin = true }
        } else {
            signinScreen(service) { wannaSignin = it }
        }
    } else {
        if (selected == null) {
            homeScreen(logado, { selectedPrisoner = it }) { usuarioLogado = null }
        } else {
            prisonerManagement(selected, logado) { selectedPrisoner = null }
        }
    }
}
