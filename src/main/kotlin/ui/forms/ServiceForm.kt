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
import backend.model.Register
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

@Composable
fun serviceForm(initial: Register? = null, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit){
    var description by remember(initial) { mutableStateOf(initial?.description()?: "") }
    var observations by remember(initial) { mutableStateOf(initial?.observations()?: "") }
    
    var loading by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf<String?>(null)}
    val scope = rememberCoroutineScope()
    
    val isFormValid = description.isNotBlank()

    
    fun save() {
        scope.launch {
            loading = true
            error = null
            try {
                withContext(Dispatchers.IO) {
                    onConfirm(
                        description,
                        observations
                    )
                }
                onDismiss()
            } catch (e: CancellationException) {
                throw e
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
        title = { Text(if (initial == null) "Cadastro de atendimento" else "Editar atendimento") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = {description = it},
                    singleLine = false,
                    label = {Text("Descrição")},
                    modifier = Modifier.fillMaxWidth()                    
                )
                OutlinedTextField(
                    value = observations,
                    onValueChange = {observations = it},
                    singleLine = false,
                    label = {Text("Observações")},
                    modifier = Modifier.fillMaxWidth()                    
                )
            }
            error?.let { Text(it, color = MaterialTheme.colors.error) }
        },
        confirmButton = {
            TextButton(
                enabled = isFormValid && !loading,
                onClick = {save()}
            ){Text("Salvar")}
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ){Text("Cancelar")}
        }
    )
}