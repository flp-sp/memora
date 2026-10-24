package ui.components.fields

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ui.components.transformation.DateTransformation

@Composable
fun DateField(
    label: String,
    digits: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = digits,
        onValueChange = { newValue -> onChange(newValue.filter { it.isDigit() }.take(8)) },
        label = { Text(label) },
        placeholder = { Text("dd/mm/aaaa") },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = DateTransformation,
        modifier = modifier.fillMaxWidth()
    )
}
