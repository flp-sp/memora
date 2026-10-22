package ui.register

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import backend.model.User
import backend.model.Prisoner
import backend.service.PrisonerService
import ui.forms.prisonerForm
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import ui.forms.userManagement

private fun dueInfo(dutyDate: LocalDate?): Pair<String, Color> {
    if (dutyDate == null) return "Sem vencimento" to Color.Gray
    val days = ChronoUnit.DAYS.between(LocalDate.now(), dutyDate)
    return when {
        days < 0L -> "Vencido há ${-days} ${if (-days == 1L) "dia" else "dias"}" to Color(0xFFC62828)
        days == 0L -> "Vence hoje" to Color(0xFFC62828)
        days <= 30L -> "Vence em $days ${if (days == 1L) "dia" else "dias"}" to Color(0xFFEF6C00)
        else -> "Vence em $days dias" to Color(0xFF2E7D32)
    }
}

private fun pendingInfo(administrativePending: String?): Pair<String, Color> =
    when (administrativePending) {
        null, "", "SEM PENDÊNCIA" -> "SEM PENDÊNCIA" to Color(0xFF2E7D32)
        "DOCUMENTO", "PEDIDO" -> administrativePending to Color(0xFFEF6C00)
        "PARECER", "DECISÃO" -> administrativePending to Color(0xFFC62828) 
        else -> administrativePending to Color.Gray
    }                  

@Composable
fun homeScreen(usuarioLogado: User,onSelectPrisoner: (Prisoner) -> Unit ,onLogout: () -> Unit) {
    val prisonerService = remember { PrisonerService() }
    var prisoners by remember { mutableStateOf(emptyList<Prisoner>()) }
    var showPrisonerForm by remember { mutableStateOf(false) }
    var editingPrisioner by remember {mutableStateOf<Prisoner?>(null)}
    var selectPrisoner by remember {mutableStateOf<Prisoner?>(null)}
    var searchQuery by remember {mutableStateOf("")}
    var showUserManagement by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    fun loadPrisoners() {
        prisoners = prisonerService.getList()
    }
    
    val filteredPrisoners = remember(prisoners, searchQuery) {
            val q = searchQuery.trim()
            if (q.isEmpty()) {
                prisoners
                } 
            else {
                val onlyNumeric = q.all { it.isDigit() || it == '.' || it == '-' }
                val qDigits = q.filter { it.isDigit()}
                prisoners.filter { p ->
                p.prisonerName().contains(q, ignoreCase = true) ||
                p.lawsuitNum().contains(q, ignoreCase = true) ||
                (onlyNumeric && qDigits.isNotEmpty() &&
                p.lawsuitNum().filter { it.isDigit() }.contains(qDigits))
            }
        }
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

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { showUserManagement = true }) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações")
        }
        OutlinedButton(onClick = onLogout) { Text("Sair") }
    }
}

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedButton(onClick = { showPrisonerForm = true }) {
                    Text("Adicionar preso")
                }
                OutlinedTextField(
    value = searchQuery,
    onValueChange = { searchQuery = it },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
    label = { Text("Pesquisar por nome ou nº do processo") },
    singleLine = true,
    trailingIcon = {
        if (searchQuery.isNotEmpty()) {
            TextButton(onClick = { searchQuery = "" }) { Text("Limpar") }
        }
    }
)

Spacer(Modifier.height(8.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
    Text("Processo", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
    Text("Nome", Modifier.weight(2f), fontWeight = FontWeight.Bold)
    Text("Último atendimento", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
    Text("Direito vencido", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
    Text("Pendência", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
    Spacer(Modifier.width(120.dp))
}

Divider()

LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
    items(filteredPrisoners, key = { it.id() }) { p ->
        val (dueText, dueColor) = dueInfo(p.dutyDate())
        val (pendingText, pendingColor) = pendingInfo(p.administrativePending())
        val formattedLastService = p.dateLastService()?.format(dateFormatter) ?: "-"

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable { onSelectPrisoner(p) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(p.lawsuitNum(), Modifier.weight(1.5f))
            Text(p.prisonerName(), Modifier.weight(2f))
            Text(formattedLastService, Modifier.weight(1.5f))
            Text(
                dueText,
                Modifier.weight(1.5f),
                color = dueColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                pendingText,
                Modifier.weight(1.5f),
                color = pendingColor,
                fontWeight = FontWeight.Bold
            )

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
                        dateLastService, dateTransferApac, jail, administrativePending ->
                if(current == null){
                    prisonerService.create(
                        lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail,
                        administrativePending
                    )
                }
                else{
                    prisonerService.update(
                        current.id(), lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail,
                        administrativePending
                    )
                }
                editingPrisioner = null
                showPrisonerForm = false
                loadPrisoners()
            }
        )
    }
    if (showUserManagement) {
    userManagement(
        onDismiss = { showUserManagement = false },
        onConfirm = { showUserManagement = false }
    )
}
}