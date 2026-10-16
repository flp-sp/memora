package ui.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import backend.model.Prisoner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.components.fields.DropdownField
import ui.components.transformation.DateTransformation
import ui.components.transformation.ProcessNumberTransformation
import java.sql.SQLException
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DIGITS_DATE = DateTimeFormatter.ofPattern("ddMMyyyy")

private val SENTENCE_TYPES = listOf("Progressão semiaberto", "Progressão aberto", "Livramento condicional")
private val JAILS = listOf("APAC", "UPTIM")
private val PENDING = listOf("Sim", "Não")

private fun sentenceTypeFromDb(value: Any?): String = when (value?.toString()) {
    "S" -> "Progressão semiaberto"
    "A" -> "Progressão aberto"
    "L" -> "Livramento condicional"
    else -> ""
}

private fun jailFromDb(value: Any?): String = when (value?.toString()) {
    "A" -> "APAC"
    "U" -> "UPTIM"
    else -> ""
}

@Composable
fun prisonerForm(
    initial: Prisoner? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, Boolean) -> Unit
) {
    var lawsuitNum by remember(initial) { mutableStateOf(initial?.lawsuitNum() ?: "") }
    var prisonerName by remember(initial) { mutableStateOf(initial?.prisonerName() ?: "") }
    var dutyDate by remember(initial) { mutableStateOf(initial?.dutyDate()?.format(DIGITS_DATE) ?: "") }
    var sentenceType by remember(initial) { mutableStateOf(sentenceTypeFromDb(initial?.sentenceType())) }
    var dateLastAnalysis by remember(initial) { mutableStateOf(initial?.dateLastAnalysis()?.format(DIGITS_DATE) ?: "") }
    var dateLastService by remember(initial) { mutableStateOf(initial?.dateLastService()?.format(DIGITS_DATE) ?: "") }
    var dateTransferApac by remember(initial) { mutableStateOf(initial?.dateTransferApac()?.format(DIGITS_DATE) ?: "") }
    var jail by remember(initial) { mutableStateOf(jailFromDb(initial?.jail())) }
    var administrativePending by remember(initial) {mutableStateOf(initial?.administrativePending() ?: false)}
    
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isFormValid = ProcessNumberTransformation.isValid(lawsuitNum) &&
            prisonerName.isNotBlank() &&
            sentenceType.isNotBlank() &&
            jail.isNotBlank() &&
            dutyDate.length == 8 &&
            dateLastAnalysis.length == 8 &&
            dateLastService.length == 8 &&
            (dateTransferApac.length == 8 || dateTransferApac.isEmpty())

    fun save() {
        scope.launch {
            loading = true
            error = null
            try {
                withContext(Dispatchers.IO) {
                    onConfirm(
                        ProcessNumberTransformation.complete(lawsuitNum),
                        prisonerName,
                        dutyDate.toFormattedDate(),
                        sentenceType,
                        dateLastAnalysis.toFormattedDate(),
                        dateLastService.toFormattedDate(),
                        dateTransferApac.toFormattedDate(),
                        jail,
                        administrativePending
                    )
                }
                onDismiss()
            } catch (e: CancellationException) {
                throw e
            } catch (e: DateTimeParseException) {
                error = "Uma das datas é inválida"
            } catch (e: SQLException) {
                error = "Erro ao salvar no banco: ${e.message}"
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text(if (initial == null) "Cadastro de preso" else "Editar preso") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lawsuitNum,
                    onValueChange = { lawsuitNum = ProcessNumberTransformation.sanitize(it) },
                    label = { Text("Nº do processo") },
                    singleLine = true,
                    isError = lawsuitNum.isNotEmpty() && !ProcessNumberTransformation.isValid(lawsuitNum),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = ProcessNumberTransformation
                )
                OutlinedTextField(
                    value = prisonerName,
                    onValueChange = { prisonerName = it },
                    label = { Text("Nome do preso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DateField("Data do direito", dutyDate) { dutyDate = it }

                DropdownField("Tipo de pena", SENTENCE_TYPES, sentenceType) { sentenceType = it }

                DateField("Data da última análise", dateLastAnalysis) { dateLastAnalysis = it }
                DateField("Data do último atendimento", dateLastService) { dateLastService = it }
                DateField("Data da transferência para APAC", dateTransferApac) { dateTransferApac = it }
                
                DropdownField("Pendencia administrativa",
                                PENDING, if(administrativePending) "Sim" else "Não") { administrativePending = it == "Sim" }

                DropdownField("Unidade prisional", JAILS, jail) { jail = it }

                error?.let { Text(it, color = MaterialTheme.colors.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isFormValid && !loading,
                onClick = { save() }
            ) { Text(if (loading) "Salvando..." else "Salvar") }
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ) { Text("Cancelar") }
        }
    )
}

/** "25122024" -> "25/12/2024" */
private fun String.toFormattedDate(): String =
    if (length == 8) "${substring(0, 2)}/${substring(2, 4)}/${substring(4)}" else this

@Composable
private fun DateField(
    label: String,
    digits: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = digits,
        onValueChange = { newValue -> onChange(newValue.filter { it.isDigit() }.take(8)) },
        label = { Text(label) },
        placeholder = { Text("dd/mm/aaaa") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = DateTransformation,
        modifier = Modifier.fillMaxWidth()
    )
}