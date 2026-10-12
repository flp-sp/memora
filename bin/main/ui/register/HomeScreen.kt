package ui.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import backend.model.User
import backend.service.PrisonerService
import ui.forms.prisonerForm

@Composable
fun homeScreen(usuarioLogado: User, onLogout: () -> Unit){
    var lawsuitNum by remember { mutableStateOf("") }
    var prisonerName by remember { mutableStateOf("") }
    var dutyDate by remember { mutableStateOf("") }
    var sentenceType by remember { mutableStateOf("") }
    var dateLastAnalysis by remember { mutableStateOf("") }
    var dateLastService by remember { mutableStateOf("") }
    var dateTransferApac by remember { mutableStateOf("") }
    var jail by remember { mutableStateOf("") }
    
    var showPrisonerForm by remember { mutableStateOf(false) }
    
    val prisonerService = remember { PrisonerService() }
    
    Column(Modifier.fillMaxSize()){
        Row(verticalAlignment = Alignment.CenterVertically){
            Text("Ola, ${usuarioLogado.userName()}")
            OutlinedButton(onClick = onLogout) {Text("Sair")}
        }
    Card(Modifier.fillMaxWidth()){
        Column(){
            OutlinedButton(onClick = {showPrisonerForm = true}){
                Text("Adicionar preso")
            }
        }
    }
    }
    if (showPrisonerForm){
        prisonerForm(
            onDismiss = {showPrisonerForm = false},
            onConfirm = { lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail ->
   
                        prisonerService.create(lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail)
                        showPrisonerForm = false
                        }
        )
    }
    
}