package ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import backend.model.User
import backend.service.UserService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sun.rmi.server.Dispatcher

@Composable
fun loginScreen(service: UserService, onLogin: (User) -> Unit){
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    val scope = rememberCoroutineScope()    
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        fun login(){
            scope.launch {  
                loading = true
                error = null
                
                try {
                    val user: User? = withContext(Dispatchers.IO){
                        service.auth(email, pass).orElse(null)
                    }
                    if (user != null){
                        onLogin(user)
                    }
                    else{
                        error = "Nome ou senha inválidos!"
                    }
                }
                catch (e: CancellationException){}
                catch (e: Exception){
                    error = e.message
                }
                loading = false
            }
        }
        
        Column {
            Text("Bem vindo ao Memora", style = MaterialTheme.typography.h1)
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("nome")}
            )
            OutlinedTextField(
                value = pass,
                onValueChange = {pass = it},
                label = {Text("senha")}
            )
            Button(
                onClick = { login() },
                enabled = !loading,
            ){Text("Entrar")}
            
            error?.let { Text(it, color = MaterialTheme.colors.error) }
        }
    }
}