package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import backend.model.Prisoner
import backend.model.User
import backend.service.UserService
import backend.service.WhitelistedUserService
import backend.storage.CredentialStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val whiteListedService = remember { WhitelistedUserService() }
    var usuarioLogado by remember { mutableStateOf<User?>(null) }
    var selectedPrisoner by remember { mutableStateOf<Prisoner?>(null) }
    var wannaSignin by remember { mutableStateOf(false) }
    var checkingAutoLogin by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val saved = withContext(Dispatchers.IO) { CredentialStorage.load() }
            if (saved != null && usuarioLogado == null) {
                val user = withContext(Dispatchers.IO) {
                    service.auth(saved.getEmail(), saved.getPassword()).orElse(null)
                }
                if (user != null && whiteListedService.checkEmailStatusLogin(user.email())) {
                    usuarioLogado = user
                } else {
                    withContext(Dispatchers.IO) { CredentialStorage.clear() }
                }
            }
        } catch (e: Exception) {
        } finally {
            checkingAutoLogin = false
        }
    }

    if (checkingAutoLogin) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

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
