package ui.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
import java.time.format.DateTimeParseException
import ui.components.fields.DropdownField
import ui.components.transformation.DateTransformation
import ui.components.transformation.ProcessNumberTransformation

@Composable
fun prisonerForm(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String) -> Unit
) {
    var lawsuitNum by remember { mutableStateOf("") }
    var prisonerName by remember { mutableStateOf("") }
    var dutyDate by remember { mutableStateOf("") }
    var sentenceType by remember { mutableStateOf("") }
    var dateLastAnalysis by remember { mutableStateOf("") }
    var dateLastService by remember { mutableStateOf("") }
    var dateTransferApac by remember { mutableStateOf("") }
    var jail by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val sentenceTypes = listOf("Progressão semiaberto", "Progressão aberto", "Livramento condicional")
    val jails = listOf("APAC", "UPTIM")

    val isFormValid = lawsuitNum.isNotBlank() &&
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
                        lawsuitNum,
                        prisonerName,
                        dutyDate.toFormattedDate(),
                        sentenceType,
                        dateLastAnalysis.toFormattedDate(),
                        dateLastService.toFormattedDate(),
                        dateTransferApac.toFormattedDate(),
                        jail
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
                if(e.message?.contains("Nenhum resultado") != true){
                error = e.message
            }else{
                onDismiss()
            }
            } finally {
                loading = false
                
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Cadastro de preso") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lawsuitNum,
                    onValueChange = { lawsuitNum = it },
                    label = { Text("Nº do processo") },
                    singleLine = true,
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

                DropdownField("Tipo de pena", sentenceTypes, sentenceType) { sentenceType = it }

                DateField("Data da última análise", dateLastAnalysis) { dateLastAnalysis = it }
                DateField("Data do último atendimento", dateLastService) { dateLastService = it }
                DateField("Data da transferência para APAC", dateTransferApac) { dateTransferApac = it }

                DropdownField("Unidade prisional", jails, jail) { jail = it }

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