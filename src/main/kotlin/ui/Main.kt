package ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import backend.model.User
import backend.service.UserService
import ui.auth.loginScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Memora",
        state = rememberWindowState(placement = WindowPlacement.Maximized)
    ) {
        MaterialTheme { app() }
    }
}
@Composable
fun app() {
    val service = remember { UserService() }
    var usuarioLogado by remember { mutableStateOf<User?>(null) }
    val logado = usuarioLogado
    
    if (logado == null) {
        loginScreen(service) { usuarioLogado = it }
    }
    else(
        Text("Deu certo")
    )
}