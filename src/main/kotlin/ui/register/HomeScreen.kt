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
import backend.service.PrisonerService
import ui.forms.prisonerForm
import java.time.format.DateTimeFormatter

@Composable
fun homeScreen(usuarioLogado: User,onSelectPrisoner: (Prisoner) -> Unit ,onLogout: () -> Unit) {
    val prisonerService = remember { PrisonerService() }
    var prisoners by remember { mutableStateOf(emptyList<Prisoner>()) }
    var showPrisonerForm by remember { mutableStateOf(false) }
    var editingPrisioner by remember {mutableStateOf<Prisoner?>(null)}
    var selectPrisoner by remember {mutableStateOf<Prisoner?>(null)}

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    fun loadPrisoners() {
        prisoners = prisonerService.getList()
    }

    LaunchedEffect(Unit) {
        loadPrisoners()
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
            Text("Olá, ${usuarioLogado.userName()}", style = MaterialTheme.typography.h6)
            OutlinedButton(onClick = onLogout) { Text("Sair") }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedButton(onClick = { showPrisonerForm = true }) {
                    Text("Adicionar preso")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text("Processo", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Text("Nome", Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Data do Direito", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(120.dp))
        }

        Divider()

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(prisoners, key = { it.id() }) { p ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { onSelectPrisoner(p) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.lawsuitNum(), Modifier.weight(1.5f))
                    Text(p.prisonerName(), Modifier.weight(2f))
                    
                    val formattedDutyDate = p.dutyDate()?.format(dateFormatter) ?: "-"
                    Text(formattedDutyDate, Modifier.weight(1.5f))

                    Row(
                        modifier = Modifier.width(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(onClick = {
                            showPrisonerForm = true
                            editingPrisioner = p
                        }) { Text("Editar") }

                        TextButton(onClick = {
                            prisonerService.delete(p.id())
                            loadPrisoners()
                        }) { Text("Excluir") }
                    }
                }
                Divider()
            }
        }
    }

    if (selectPrisoner != null){
        val current = selectPrisoner
    }
    
    if (showPrisonerForm) {
        val current = editingPrisioner
            
        prisonerForm(
            initial = current,
            onDismiss = { showPrisonerForm = false; editingPrisioner = null },
            onConfirm = { lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail ->
                if(current == null){
                    prisonerService.create(
                        lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail
                    )
                }
                else{
                    prisonerService.update(
                        current.id(), lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail
                    )
                }
                editingPrisioner = null
                showPrisonerForm = false
                loadPrisoners()
            }
        )
    }
}