package ui.components.transformation

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

object ProcessNumberTransformation : VisualTransformation {
    private const val BASE_LEN = 13
    private const val DEFAULT_SUFFIX = "8100060"
    private const val TOTAL_LEN = 20
    private val SEPARATORS = mapOf(7 to '-', 9 to '.', 13 to '.', 14 to '.', 16 to '.')

    override fun filter(text: AnnotatedString): TransformedText {
        val typed = text.text.take(TOTAL_LEN)
        val full = if (typed.length < BASE_LEN) typed
                   else typed + DEFAULT_SUFFIX.drop(typed.length - BASE_LEN)

        val isDigitAt = mutableListOf<Boolean>()
        val formatted = buildAnnotatedString {
            full.forEachIndexed { i, c ->
                SEPARATORS[i]?.let {
                    append(it)
                    isDigitAt.add(false)
                }
                if (i >= typed.length) {
                    withStyle(SpanStyle(color = Color.Gray)) { append(c) }
                } else {
                    append(c)
                }
                isDigitAt.add(true)
            }
        }

        val digitsBefore = IntArray(isDigitAt.size + 1)
        isDigitAt.forEachIndexed { t, isDigit ->
            digitsBefore[t + 1] = digitsBefore[t] + if (isDigit) 1 else 0
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                offset + SEPARATORS.keys.count { it < offset }

            override fun transformedToOriginal(offset: Int): Int =
                digitsBefore[offset.coerceIn(0, isDigitAt.size)].coerceAtMost(typed.length)
        }
        return TransformedText(formatted, mapping)
    }
}