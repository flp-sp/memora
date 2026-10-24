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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import backend.model.Prisoner
import backend.model.Register
import backend.model.User
import backend.service.RegisterService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.components.dialogs.ConfirmDialog
import ui.components.displays.PrisonerDetails
import ui.components.layout.AppHeader
import ui.forms.serviceForm
import java.time.format.DateTimeFormatter

private val REGISTER_DATE_WIDTH = 150.dp
private const val REGISTER_DESCRIPTION_WEIGHT = 2f
private const val REGISTER_OBSERVATION_WEIGHT = 2f
private val REGISTER_AUTHOR_WIDTH = 140.dp
private val REGISTER_EDIT_WIDTH = 150.dp
private val REGISTER_ACTIONS_WIDTH = 150.dp

fun String.preview(maxChars: Int = 60): String {
    val oneLine = this.replace(Regex("\\s+"), " ").trim()
    return if (oneLine.length > maxChars) oneLine.take(maxChars).trimEnd() + "..." else oneLine
}

@Composable
fun prisonerManagement(selectedPrisoner: Prisoner, userLogado: User, onExit: () -> Unit) {
    var showManagementForm by remember { mutableStateOf(false) }
    var editingRegister by remember { mutableStateOf<Register?>(null) }
    var registers by remember { mutableStateOf(emptyList<Register>()) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var registerToDelete by remember { mutableStateOf<Register?>(null) }
    var deleteLoading by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val registerService = remember { RegisterService() }
    val scope = rememberCoroutineScope()

    fun loadList() {
        registers = registerService.getList(selectedPrisoner.id())
    }

    LaunchedEffect(selectedPrisoner.id()) {
        try {
            registers = withContext(Dispatchers.IO) { registerService.getList(selectedPrisoner.id()) }
            loadError = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            loadError = "Não foi possível carregar os atendimentos."
        }
    }

    fun confirmDelete() {
        val target = registerToDelete ?: return
        scope.launch {
            deleteLoading = true
            deleteError = null
            try {
                withContext(Dispatchers.IO) {
                    registerService.delete(target.id(), userLogado.id())
                }
                registerToDelete = null
                registers = withContext(Dispatchers.IO) {
                    registerService.getList(selectedPrisoner.id())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                deleteError = "Não foi possível excluir o atendimento. Tente novamente."
            } finally {
                deleteLoading = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppHeader(
            title = selectedPrisoner.prisonerName(),
            subtitle = "Processo ${selectedPrisoner.lawsuitNum()}",
            onBack = onExit
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrisonerDetails(
                prisoner = selectedPrisoner,
                modifier = Modifier.fillMaxWidth()
            )

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Atendimentos", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${registers.size} ${if (registers.size == 1) "registro" else "registros"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { showManagementForm = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar atendimento")
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
                    Text(
                        "Data de criação",
                        Modifier.width(REGISTER_DATE_WIDTH),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "Descrição",
                        Modifier.weight(REGISTER_DESCRIPTION_WEIGHT),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "Observação",
                        Modifier.weight(REGISTER_OBSERVATION_WEIGHT),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "Autor",
                        Modifier.width(REGISTER_AUTHOR_WIDTH),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "Última edição",
                        Modifier.width(REGISTER_EDIT_WIDTH),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(REGISTER_ACTIONS_WIDTH))
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (registers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum atendimento registrado.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(registers, key = { _, r -> r.id() }) { index, r ->
                        val editorName = if (r.idUserEdit() > 0) {
                            r.userNameEdit() ?: "—"
                        } else {
                            "Não editado"
                        }

                        Column(
                            modifier = Modifier.background(
                                if (index % 2 == 0) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingRegister = r
                                        showManagementForm = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    r.dateRegister().format(dateTimeFormatter),
                                    Modifier.width(REGISTER_DATE_WIDTH),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    r.description().preview(70),
                                    Modifier.weight(REGISTER_DESCRIPTION_WEIGHT),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    r.observations().preview(70),
                                    Modifier.weight(REGISTER_OBSERVATION_WEIGHT),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    r.userName() ?: "—",
                                    Modifier.width(REGISTER_AUTHOR_WIDTH),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    editorName,
                                    Modifier.width(REGISTER_EDIT_WIDTH),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.width(REGISTER_ACTIONS_WIDTH),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = {
                                        editingRegister = r
                                        showManagementForm = true
                                    }) {
                                        Text("Editar")
                                    }
                                    TextButton(
                                        onClick = {
                                            registerToDelete = r
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

    if (showManagementForm) {
        val current = editingRegister
        serviceForm(
            initial = current,
            onDismiss = {
                showManagementForm = false
                editingRegister = null
            },
            onConfirm = { description, observations ->
                if (current == null) {
                    registerService.create(
                        userLogado.id(),
                        selectedPrisoner.id(),
                        description,
                        observations
                    )
                } else {
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

    val target = registerToDelete
    if (target != null) {
        ConfirmDialog(
            title = "Excluir atendimento",
            message = "Deseja excluir este atendimento? Esta ação não pode ser desfeita.",
            loading = deleteLoading,
            error = deleteError,
            onConfirm = { confirmDelete() },
            onDismiss = {
                if (!deleteLoading) {
                    registerToDelete = null
                    deleteError = null
                }
            }
        )
    }
}
