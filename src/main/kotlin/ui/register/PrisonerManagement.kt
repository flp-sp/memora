package ui.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import backend.model.User
import backend.model.Prisoner
import backend.model.Register
import backend.service.PrisonerService
import backend.service.RegisterService
import ui.components.displays.PrisonerDetails
import ui.forms.prisonerForm
import ui.forms.serviceForm
import java.time.format.DateTimeFormatter

fun String.preview(maxChars: Int = 60): String {
        val oneLine = this.replace(Regex("\\s+"), " ").trim()
        return if (oneLine.length > maxChars) oneLine.take(maxChars).trimEnd() + "..." else oneLine
    }

@Composable
fun prisonerManagement(selectedPrisoner: Prisoner, userLogado: User, onExit: () -> Unit){
    var showManagementForm by remember { mutableStateOf(false) }
    var editingRegister by remember {mutableStateOf<Register?>(null)}
    var registers by remember {mutableStateOf(emptyList<Register>())}
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    var editUserName by remember { mutableStateOf("") }
    
    var registerService = remember { RegisterService() }
    
    fun loadList(){
        registers = registerService.getList(selectedPrisoner.id())
    }
    
    LaunchedEffect(Unit){
        loadList()
    }
    
    Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Olá, ${ userLogado.userName() }", style = MaterialTheme.typography.h6)
        OutlinedButton(onClick = onExit) { Text("Voltar") }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            OutlinedButton(onClick = { showManagementForm = true }) {
                Text("Adicionar agendamento")
            }
        }
    }
    Divider()
    PrisonerDetails(
        prisoner = selectedPrisoner,
        modifier = Modifier.fillMaxWidth().weight(1f)
    )
    Divider()
    
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text("Data de Criação", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Text("Descriçao", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Observação", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Text("Autoria", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Text("Última edição", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(120.dp))
        }
    
    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(registers, key = { it.id() }) { r ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { editingRegister = r; showManagementForm = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    
                    Text(r.dateRegister().format(dateTimeFormatter), Modifier.weight(1.5f))
                    Text(r.description().preview(60), Modifier.weight(2f), maxLines = 1)
                    Text(r.observations().preview(60), Modifier.weight(2f), maxLines = 1)
                    Text(r.userName(), Modifier.weight(2f))
                    if (r.idUserEdit() > 0){
                        editUserName = r.userNameEdit()
                    }
                    else {
                        editUserName = "Não editado"
                    }
                    Text(editUserName, Modifier.weight(2f))

                    Row(
                        modifier = Modifier.width(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(onClick = {
                            registerService.delete(r.id(), userLogado.id())
                            loadList()
                        }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    
    if (showManagementForm){
        val current = editingRegister
        serviceForm(
            initial = current,
            onDismiss = {showManagementForm = false; editingRegister = null},
            onConfirm = {description, observations ->
                if (current == null){
                    registerService.create(
                            userLogado.id(),
                        selectedPrisoner.id(),
                                    description,
                                    observations
                    )
                }
                else{
                    registerService.update(
                                        current.id(), 
                                userLogado.id(),
                                        description,
                                        observations
                    )
                }
                editingRegister = null
                showManagementForm = false
                loadList()
            }
        )   
    }
}