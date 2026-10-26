package ui.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ui.components.fields.DateField
import ui.components.fields.DropdownField
import ui.components.transformation.ProcessNumberTransformation
import java.sql.SQLException
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DIGITS_DATE = DateTimeFormatter.ofPattern("ddMMyyyy")

private val SENTENCE_TYPES = listOf("Progressão semiaberto", "Progressão aberto", "Livramento condicional")
private val JAILS = listOf("APAC", "UPTIM", "Regime Aberto")
private val PENDING = listOf("SEM PENDÊNCIA", "DOCUMENTO", "PEDIDO", "PARECER", "DECISÃO")

private fun sentenceTypeFromDb(value: Any?): String = when (value?.toString()) {
    "S" -> "Progressão semiaberto"
    "A" -> "Progressão aberto"
    "L" -> "Livramento condicional"
    else -> ""
}

private fun jailFromDb(value: Any?): String = when (value?.toString()) {
    "A" -> "APAC"
    "U" -> "UPTIM"
    "R" -> "Regime Aberto"
    else -> ""
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FormError(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
fun prisonerForm(
    initial: Prisoner? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    var lawsuitNum by remember(initial) { mutableStateOf(initial?.lawsuitNum()?.takeIf { it.isNotBlank() } ?: "") }
    var prisonerName by remember(initial) { mutableStateOf(initial?.prisonerName() ?: "") }
    var dutyDate by remember(initial) { mutableStateOf(initial?.dutyDate()?.format(DIGITS_DATE) ?: "") }
    var sentenceType by remember(initial) { mutableStateOf(sentenceTypeFromDb(initial?.sentenceType()).takeIf { it.isNotBlank() } ?: "") }
    var dateLastAnalysis by remember(initial) { mutableStateOf(initial?.dateLastAnalysis()?.format(DIGITS_DATE) ?: "") }
    var dateLastService by remember(initial) { mutableStateOf(initial?.dateLastService()?.format(DIGITS_DATE) ?: "") }
    var dateTransferApac by remember(initial) { mutableStateOf(initial?.dateTransferApac()?.format(DIGITS_DATE) ?: "") }
    var jail by remember(initial) { mutableStateOf(jailFromDb(initial?.jail()).takeIf { it.isNotBlank() } ?: "") }
    var administrativePending by remember(initial) {
        val ap = initial?.administrativePending()?.toString()
        mutableStateOf(ap?.takeIf { it.isNotBlank() && it in PENDING } ?: PENDING.first())
    }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val isFormValid = (lawsuitNum.isEmpty() || ProcessNumberTransformation.isValid(lawsuitNum)) &&
        prisonerName.isNotBlank() &&
        (dutyDate.isEmpty() || dutyDate.length == 8) &&
        (dateLastAnalysis.isEmpty() || dateLastAnalysis.length == 8) &&
        (dateLastService.isEmpty() || dateLastService.length == 8) &&
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
                error = "Uma das datas é inválida."
            } catch (e: SQLException) {
                error = "Não foi possível salvar o preso. Verifique os dados e tente novamente."
            } catch (e: Exception) {
                error = "Não foi possível salvar o preso. Tente novamente."
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
                modifier = Modifier
                    .widthIn(min = 520.dp, max = 640.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionTitle("Identificação")
                OutlinedTextField(
                    value = lawsuitNum,
                    onValueChange = { lawsuitNum = ProcessNumberTransformation.sanitize(it) },
                    label = { Text("Nº do processo") },
                    singleLine = true,
                    isError = lawsuitNum.isNotEmpty() && !ProcessNumberTransformation.isValid(lawsuitNum),
                    supportingText = if (lawsuitNum.isNotEmpty() && !ProcessNumberTransformation.isValid(lawsuitNum)) {
                        { Text("Informe o número completo do processo.") }
                    } else {
                        null
                    },
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SectionTitle("Prazos e datas")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateField(
                        label = "Data do direito",
                        digits = dutyDate,
                        modifier = Modifier.weight(1f),
                        isError = dutyDate.isNotEmpty() && dutyDate.length != 8,
                        onChange = { dutyDate = it }
                    )
                    DateField(
                        label = "Data da última análise",
                        digits = dateLastAnalysis,
                        modifier = Modifier.weight(1f),
                        isError = dateLastAnalysis.isNotEmpty() && dateLastAnalysis.length != 8,
                        onChange = { dateLastAnalysis = it }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateField(
                        label = "Data do último atendimento",
                        digits = dateLastService,
                        modifier = Modifier.weight(1f),
                        isError = dateLastService.isNotEmpty() && dateLastService.length != 8,
                        onChange = { dateLastService = it }
                    )
                    DateField(
                        label = "Data da transferência para APAC",
                        digits = dateTransferApac,
                        modifier = Modifier.weight(1f),
                        isError = dateTransferApac.isNotEmpty() && dateTransferApac.length != 8,
                        onChange = { dateTransferApac = it }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SectionTitle("Classificação")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        DropdownField("Tipo de pena", SENTENCE_TYPES, sentenceType) { sentenceType = it }
                    }
                    Column(Modifier.weight(1f)) {
                        DropdownField("Unidade prisional", JAILS, jail) { jail = it }
                    }
                }
                DropdownField("Pendência administrativa", PENDING, administrativePending) {
                    administrativePending = it
                }

                error?.let { FormError(it) }
            }
        },
        confirmButton = {
            Button(
                enabled = isFormValid && !loading,
                onClick = { save() }
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

/** "25122024" -> "25/12/2024" */
private fun String.toFormattedDate(): String =
    if (length == 8) "${substring(0, 2)}/${substring(2, 4)}/${substring(4)}" else this
