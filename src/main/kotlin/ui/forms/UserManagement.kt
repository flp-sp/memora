package ui.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import backend.service.UserService
import backend.service.WhitelistedUserService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.components.fields.DropdownField
import java.util.concurrent.atomic.AtomicInteger

private val STATUS_OPTIONS = linkedMapOf(
    'A' to "Ativado",
    'C' to "Cancelado",
    'O' to "Aberto"
)

private val ROLE_OPTIONS = linkedMapOf(
    'A' to "Administrador",
    'U' to "Usuário"
)

private const val DEFAULT_STATUS = 'O'
private const val DEFAULT_ROLE = 'U'

private data class UserRow(
    val id: Int,
    val email: String,
    val userName: String?,
    val status: Char,
    val sysRole: Char,
    val inWhitelist: Boolean,
    val isNew: Boolean = false
)

private fun key(email: String) = email.trim().lowercase()

private fun looksLikeEmail(email: String): Boolean {
    val e = email.trim()
    return e.contains('@') && e.substringAfter('@').contains('.') && !e.contains(' ')
}

@Composable
fun userManagement(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }
    val userService = remember { UserService() }
    val whitelistedUserService = remember { WhitelistedUserService() }
    val scope = rememberCoroutineScope()
    val idGen = remember { AtomicInteger(0) }

    val rows = remember { mutableStateListOf<UserRow>() }
    var original by remember { mutableStateOf<Map<Int, UserRow>>(emptyMap()) }

    var newEmail by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf(DEFAULT_STATUS) }
    var newRole by remember { mutableStateOf(DEFAULT_ROLE) }
    var newEmailError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val loaded = withContext(Dispatchers.IO) {
                val users = userService.getList()
                val whitelist = whitelistedUserService.getList()

                val userByEmail = users.associateBy { key(it.email()) }
                val whitelistKeys = whitelist.map { key(it.email()) }.toSet()

                val fromWhitelist = whitelist.map { w ->
                    val u = userByEmail[key(w.email())]
                    UserRow(
                        id = idGen.getAndIncrement(),
                        email = w.email(),
                        userName = u?.userName(),
                        status = w.currentStatus(),
                        sysRole = w.sysRole(),
                        inWhitelist = true
                    )
                }
                val usersOnly = users
                    .filter { key(it.email()) !in whitelistKeys }
                    .map { u ->
                        UserRow(
                            id = idGen.getAndIncrement(),
                            email = u.email(),
                            userName = u.userName(),
                            status = 'A',
                            sysRole = DEFAULT_ROLE,
                            inWhitelist = false
                        )
                    }

                (fromWhitelist + usersOnly).sortedBy { key(it.email) }
            }
            rows.clear()
            rows.addAll(loaded)
            original = loaded.associateBy { it.id }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "Não foi possível carregar os usuários."
        } finally {
            loading = false
        }
    }

    fun emailError(r: UserRow): String? {
        if (!looksLikeEmail(r.email)) return "E-mail inválido"
        val dup = rows.any { it.id != r.id && key(it.email) == key(r.email) }
        return if (dup) "Este e-mail já existe na lista" else null
    }

    fun changedRows(): List<UserRow> = rows.filter { r ->
        if (r.isNew) return@filter true
        val o = original[r.id] ?: return@filter false
        key(o.email) != key(r.email) || o.status != r.status || o.sysRole != r.sysRole
    }

    fun allValid(): Boolean = rows.all { emailError(it) == null }

    fun addNew() {
        val candidate = UserRow(
            id = idGen.getAndIncrement(),
            email = newEmail.trim(),
            userName = null,
            status = newStatus,
            sysRole = newRole,
            inWhitelist = false,
            isNew = true
        )
        val err = emailError(candidate)
        if (err != null) {
            newEmailError = err
            return
        }
        rows.add(0, candidate)
        newEmail = ""
        newStatus = DEFAULT_STATUS
        newRole = DEFAULT_ROLE
        newEmailError = null
    }

    fun save() {
        scope.launch {
            loading = true
            error = null
            try {
                withContext(Dispatchers.IO) {
                    changedRows().forEach { r ->
                        val email = r.email.trim()
                        val o = original[r.id]
                        when {
                            r.isNew || !r.inWhitelist ->
                                whitelistedUserService.create(email, r.status, r.sysRole)

                            o != null && key(o.email) != key(email) ->
                                whitelistedUserService.update(o.email, r.status, r.sysRole)

                            else ->
                                whitelistedUserService.update(email, r.status, r.sysRole)
                        }
                    }
                }
                showConfirm = false
                onConfirm()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "Não foi possível salvar as alterações."
                showConfirm = false
            } finally {
                loading = false
            }
        }
    }

    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(
            modifier = Modifier.width(860.dp).heightIn(max = 780.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Gerenciar usuários", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Controle quem pode acessar o sistema.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss, enabled = !loading) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Autorizar novo e-mail", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = {
                                newEmail = it
                                newEmailError = null
                            },
                            label = { Text("Novo e-mail") },
                            singleLine = true,
                            isError = newEmailError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        newEmailError?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                DropdownField(
                                    label = "Situação",
                                    options = STATUS_OPTIONS.values.toList(),
                                    selected = STATUS_OPTIONS[newStatus] ?: newStatus.toString(),
                                    onSelected = { label ->
                                        newStatus = STATUS_OPTIONS.entries.first { it.value == label }.key
                                    }
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                DropdownField(
                                    label = "Perfil",
                                    options = ROLE_OPTIONS.values.toList(),
                                    selected = ROLE_OPTIONS[newRole] ?: newRole.toString(),
                                    onSelected = { label ->
                                        newRole = ROLE_OPTIONS.entries.first { it.value == label }.key
                                    }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(enabled = !loading, onClick = { addNew() }) {
                                Text("Adicionar à lista")
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "O usuário poderá se cadastrar com este e-mail.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("E-mails autorizados", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${rows.size} ${if (rows.size == 1) "registro" else "registros"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (loading && rows.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }

                    rows.forEachIndexed { index, row ->
                        val err = emailError(row)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (row.isNew) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            row.userName ?: if (row.isNew) "Novo e-mail (não salvo)" else "Sem conta cadastrada",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!row.inWhitelist && !row.isNew) {
                                            Text(
                                                "Sem autorização na lista de e-mails",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    if (row.isNew) {
                                        TextButton(onClick = { rows.removeAt(index) }) {
                                            Text("Remover")
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = row.email,
                                    onValueChange = { rows[index] = rows[index].copy(email = it) },
                                    label = { Text("E-mail") },
                                    singleLine = true,
                                    isError = err != null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                err?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        DropdownField(
                                            label = "Situação",
                                            options = STATUS_OPTIONS.values.toList(),
                                            selected = STATUS_OPTIONS[row.status] ?: row.status.toString(),
                                            onSelected = { label ->
                                                val c = STATUS_OPTIONS.entries.first { it.value == label }.key
                                                rows[index] = rows[index].copy(status = c)
                                            }
                                        )
                                    }
                                    Column(Modifier.weight(1f)) {
                                        DropdownField(
                                            label = "Perfil",
                                            options = ROLE_OPTIONS.values.toList(),
                                            selected = ROLE_OPTIONS[row.sysRole] ?: row.sysRole.toString(),
                                            onSelected = { label ->
                                                val c = ROLE_OPTIONS.entries.first { it.value == label }.key
                                                rows[index] = rows[index].copy(sysRole = c)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            it,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(enabled = !loading, onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        enabled = !loading && changedRows().isNotEmpty() && allValid(),
                        onClick = { showConfirm = true }
                    ) {
                        Text("Salvar alterações")
                    }
                }
            }
        }
    }

    if (showConfirm) {
        val changes = changedRows()
        AlertDialog(
            onDismissRequest = { if (!loading) showConfirm = false },
            title = { Text("Confirmar alterações") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Deseja aplicar as alterações em ${changes.size} registro(s)?")
                    changes.forEach { r ->
                        val o = original[r.id]
                        Column {
                            if (r.isNew || o == null) {
                                Text("Novo: ${r.email.trim()}", fontWeight = FontWeight.Bold)
                                Text("Situação: ${STATUS_OPTIONS[r.status]}  |  Perfil: ${ROLE_OPTIONS[r.sysRole]}")
                            } else {
                                Text(o.email, fontWeight = FontWeight.Bold)
                                if (key(o.email) != key(r.email)) {
                                    Text("E-mail: ${o.email} → ${r.email.trim()}")
                                }
                                if (o.status != r.status) {
                                    Text("Situação: ${STATUS_OPTIONS[o.status]} → ${STATUS_OPTIONS[r.status]}")
                                }
                                if (o.sysRole != r.sysRole) {
                                    Text("Perfil: ${ROLE_OPTIONS[o.sysRole]} → ${ROLE_OPTIONS[r.sysRole]}")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(enabled = !loading, onClick = { save() }) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirmar")
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !loading, onClick = { showConfirm = false }) {
                    Text("Voltar")
                }
            }
        )
    }
}
