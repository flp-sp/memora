package ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import backend.model.User
import backend.model.WhiteListed
import backend.service.UserService
import backend.service.WhitelistedUserService
import backend.validation.ParameterValidation
import com.sun.org.apache.xpath.internal.operations.Bool
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sun.rmi.server.Dispatcher

@Composable
fun signinScreen(service: UserService, onSignin: (Boolean) -> Unit){
    var name by remember {mutableStateOf("")}
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    val scope = rememberCoroutineScope()

    var whiteListedService = remember { WhitelistedUserService() }
    
    /*
    LaunchedEffect(Unit){
        try{
            withContext(Dispatchers.IO){
                service.ensureFirstUser()
            }
        }
        catch (e: CancellationException){}
        catch (e: Exception){
            if(e.message?.contains("conexão") == true)
            {
                error = "Falha na conexão: Verifique sua conexão a internet."    
            }
            else{
                error = e.message
            }
        }
    }*/
    
    fun signin(){
            scope.launch {  
                loading = true
                error = null
                
                if(ParameterValidation.validateEmail(email) && ParameterValidation.validatePass(pass)
                    && ParameterValidation.validateNome(name))
                {
                    try {
                        val autorizado: Boolean = withContext(Dispatchers.IO){
                            whiteListedService.checkEmailStatus(email)
                        }
                        if (autorizado){
                            service.create(name, email, pass)
                            onSignin(true)
                        }
                        else{
                            error = "Email desconhecido, contate o suporte"
                        }
                    }
                    catch (e: CancellationException){}
                    catch (e: Exception){
                        error = e.message
                    }
                }
                else{
                    error = "Credenciais inválidas"
                }
                loading = false
            }
        }
    
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center){        
        Column {
            Text("Bem vindo ao Memora", style = MaterialTheme.typography.h1)
            OutlinedTextField(
                value = name,
                onValueChange = {name = it},
                label = {Text("Nome")},
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                label = {Text("Email")},
                singleLine = true
            )
            OutlinedTextField(
                value = pass,
                onValueChange = {pass = it},
                label = {Text("Senha")},
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = { signin() },
                enabled = !loading,
            ){Text("Cadastrar")}
            
            error?.let { Text(it, color = MaterialTheme.colors.error) }
        }
    }
}