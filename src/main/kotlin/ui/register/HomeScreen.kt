package ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import backend.model.Prisoner
import backend.model.User
import backend.service.PrisonerService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.components.dialogs.ConfirmDialog
import ui.components.displays.dueInfo
import ui.components.displays.pendingInfo
import ui.components.layout.AppHeader
import ui.forms.prisonerForm
import ui.forms.userManagement
import java.time.format.DateTimeFormatter

private const val PROCESS_WEIGHT = 1.4f
private const val NAME_WEIGHT = 2.1f
private const val SERVICE_WEIGHT = 1.3f
private const val DUE_WEIGHT = 1.5f
private const val PENDING_WEIGHT = 1.3f
private val ACTIONS_WIDTH = 176.dp

private fun isAdmin(user: User): Boolean = user.sysRole() == 'A'

@Composable
fun homeScreen(usuarioLogado: User, onSelectPrisoner: (Prisoner) -> Unit, onLogout: () -> Unit) {
    val prisonerService = remember { PrisonerService() }
    var prisoners by remember { mutableStateOf(emptyList<Prisoner>()) }
    var showPrisonerForm by remember { mutableStateOf(false) }
    var editingPrisoner by remember { mutableStateOf<Prisoner?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showUserManagement by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var prisonerToDelete by remember { mutableStateOf<Prisoner?>(null) }
    var deleteLoading by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    fun loadPrisoners() {
        prisoners = prisonerService.getList()
    }

    val filteredPrisoners = remember(prisoners, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            prisoners
        } else {
            val onlyNumeric = q.all { it.isDigit() || it == '.' || it == '-' }
            val qDigits = q.filter { it.isDigit() }
            prisoners.filter { p ->
                p.prisonerName().contains(q, ignoreCase = true) ||
                    p.lawsuitNum().contains(q, ignoreCase = true) ||
                    (onlyNumeric && qDigits.isNotEmpty() &&
                        p.lawsuitNum().filter { it.isDigit() }.contains(qDigits))
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            prisoners = withContext(Dispatchers.IO) { prisonerService.getList() }
            loadError = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            loadError = "Não foi possível carregar a lista de presos."
        }
    }

    fun confirmDelete() {
        val target = prisonerToDelete ?: return
        scope.launch {
            deleteLoading = true
            deleteError = null
            try {
                withContext(Dispatchers.IO) { prisonerService.delete(target.id()) }
                prisonerToDelete = null
                prisoners = withContext(Dispatchers.IO) { prisonerService.getList() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteError = "Não foi possível excluir o preso. Tente novamente."
            } finally {
                deleteLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppHeader(
            title = "Memora",
            subtitle = "Olá, ${usuarioLogado.userName()} · ${if (isAdmin(usuarioLogado)) "Administrador" else "Usuário"}",
            actions = {
                if (isAdmin(usuarioLogado)) {
                    TextButton(
                        onClick = { showUserManagement = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Usuários")
                    }
                }
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Sair")
                }
            }
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Pesquisar por nome ou nº do processo") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpar pesquisa")
                                }
                            }
                        },
                        singleLine = true
                    )
                    Button(
                        onClick = { showPrisonerForm = true },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Adicionar preso")
                    }
                }
            }

            if (loadError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        loadError.orEmpty(),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Processo", Modifier.weight(PROCESS_WEIGHT), style = MaterialTheme.typography.labelLarge)
                    Text("Nome", Modifier.weight(NAME_WEIGHT), style = MaterialTheme.typography.labelLarge)
                    Text("Último atendimento", Modifier.weight(SERVICE_WEIGHT), style = MaterialTheme.typography.labelLarge)
                    Text("Direito vencido", Modifier.weight(DUE_WEIGHT), style = MaterialTheme.typography.labelLarge)
                    Text("Pendência", Modifier.weight(PENDING_WEIGHT), style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(ACTIONS_WIDTH))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (filteredPrisoners.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (prisoners.isEmpty()) "Nenhum preso cadastrado."
                                    else "Nenhum resultado encontrado para \"$searchQuery\".",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (prisoners.isEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { showPrisonerForm = true }) {
                                        Text("Cadastrar primeiro preso")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(filteredPrisoners, key = { _, p -> p.id() }) { index, p ->
                        val (dueText, dueColor) = dueInfo(p.dutyDate())
                        val (pendingText, pendingColor) = pendingInfo(p.administrativePending())
                        val formattedLastService = p.dateLastService()?.format(dateFormatter) ?: "—"

                        Column(
                            modifier = Modifier.background(
                                if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPrisoner(p) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    p.lawsuitNum(),
                                    Modifier.weight(PROCESS_WEIGHT),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    p.prisonerName(),
                                    Modifier.weight(NAME_WEIGHT),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(formattedLastService, Modifier.weight(SERVICE_WEIGHT))
                                Text(
                                    dueText,
                                    Modifier.weight(DUE_WEIGHT),
                                    color = dueColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    pendingText,
                                    Modifier.weight(PENDING_WEIGHT),
                                    color = pendingColor,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.width(ACTIONS_WIDTH),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = {
                                        editingPrisoner = p
                                        showPrisonerForm = true
                                    }) {
                                        Text("Editar")
                                    }
                                    TextButton(
                                        onClick = {
                                            prisonerToDelete = p
                                            deleteError = null
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Excluir")
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }

    if (showPrisonerForm) {
        val current = editingPrisoner

        prisonerForm(
            initial = current,
            onDismiss = {
                showPrisonerForm = false
                editingPrisoner = null
            },
            onConfirm = { lawsuitNum, prisonerName, dutyDate,
                          sentenceType, dateLastAnalysis,
                          dateLastService, dateTransferApac, jail, administrativePending ->
                if (current == null) {
                    prisonerService.create(
                        lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail,
                        administrativePending
                    )
                } else {
                    prisonerService.update(
                        current.id(), lawsuitNum, prisonerName, dutyDate,
                        sentenceType, dateLastAnalysis,
                        dateLastService, dateTransferApac, jail,
                        administrativePending
                    )
                }
                editingPrisoner = null
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

    val target = prisonerToDelete
    if (target != null) {
        ConfirmDialog(
            title = "Excluir preso",
            message = "Deseja excluir \"${target.prisonerName()}\"? Esta ação não pode ser desfeita.",
            loading = deleteLoading,
            error = deleteError,
            onConfirm = { confirmDelete() },
            onDismiss = {
                if (!deleteLoading) {
                    prisonerToDelete = null
                    deleteError = null
                }
            }
        )
    }
}
