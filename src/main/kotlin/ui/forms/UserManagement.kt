package ui.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    'A' to "Admin",
    'U' to "User"
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

    // Campos de "novo email"
    var newEmail by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf(DEFAULT_STATUS) }
    var newRole by remember { mutableStateOf(DEFAULT_ROLE) }
    var newEmailError by remember { mutableStateOf<String?>(null) }

    // Carrega Users + WhiteListed (união, relacionados pelo email)
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
                            status = 'A',   // já tem conta
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
            error = "Erro ao carregar usuários: ${e.message}"
        } finally {
            loading = false
        }
    }

    fun emailError(r: UserRow): String? {
        if (!looksLikeEmail(r.email)) return "Email inválido"
        val dup = rows.any { it.id != r.id && key(it.email) == key(r.email) }
        return if (dup) "Email já existe na lista" else null
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
                error = "Erro ao salvar: ${e.message}"
                showConfirm = false
            } finally {
                loading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Gestor de usuários") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Text("Adicionar email", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it; newEmailError = null },
                    label = { Text("Novo email") },
                    singleLine = true,
                    isError = newEmailError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                newEmailError?.let {
                    Text(it, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.error)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        DropdownField(
                            label = "Status",
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
                OutlinedButton(enabled = !loading, onClick = { addNew() }) { Text("Adicionar à lista") }

                Divider()

                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (loading && rows.isEmpty()) {
                        CircularProgressIndicator()
                    }
                    rows.forEachIndexed { index, row ->
                        val err = emailError(row)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    row.userName ?: if (row.isNew) "Novo (não salvo)" else "Sem conta ainda",
                                    fontWeight = FontWeight.Bold
                                )
                                if (row.isNew) {
                                    TextButton(onClick = { rows.removeAt(index) }) { Text("Remover") }
                                }
                            }
                            OutlinedTextField(
                                value = row.email,
                                onValueChange = { rows[index] = rows[index].copy(email = it) },
                                label = { Text("Email") },
                                singleLine = true,
                                isError = err != null,
                                modifier = Modifier.fillMaxWidth()
                            )
                            err?.let {
                                Text(it, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.error)
                            }
                            if (!row.inWhitelist && !row.isNew) {
                                Text(
                                    "Sem registro na whitelist",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.error
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    DropdownField(
                                        label = "Status",
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
                            Divider()
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colors.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading && changedRows().isNotEmpty() && allValid(),
                onClick = { showConfirm = true }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showConfirm) {
        val changes = changedRows()
        AlertDialog(
            onDismissRequest = { if (!loading) showConfirm = false },
            title = { Text("Confirmar alterações") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Deseja aplicar as alterações em ${changes.size} registro(s)?")
                    changes.forEach { r ->
                        val o = original[r.id]
                        Column {
                            if (r.isNew || o == null) {
                                Text("Novo: ${r.email.trim()}", fontWeight = FontWeight.Bold)
                                Text("Status: ${STATUS_OPTIONS[r.status]}  |  Perfil: ${ROLE_OPTIONS[r.sysRole]}")
                            } else {
                                Text(o.email, fontWeight = FontWeight.Bold)
                                if (key(o.email) != key(r.email)) {
                                    Text("Email: ${o.email} → ${r.email.trim()}")
                                }
                                if (o.status != r.status) {
                                    Text("Status: ${STATUS_OPTIONS[o.status]} → ${STATUS_OPTIONS[r.status]}")
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
                TextButton(enabled = !loading, onClick = { save() }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(enabled = !loading, onClick = { showConfirm = false }) { Text("Voltar") }
            }
        )
    }
}