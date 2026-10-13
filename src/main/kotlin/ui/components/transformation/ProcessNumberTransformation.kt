package ui.components.transformation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

object ProcessNumberTransformation : VisualTransformation {
    private const val BASE_LEN = 13
    private const val DEFAULT_SUFFIX = "8100060"
    const val TOTAL_LEN = 20
    private val SEPARATORS = mapOf(7 to '-', 9 to '.', 13 to '.', 14 to '.', 16 to '.')

    /** Mantém só dígitos e limita ao tamanho máximo. Use no onValueChange. */
    fun sanitize(input: String): String =
        input.filter { it.isDigit() }.take(TOTAL_LEN)

    /** Retorna o número completo, com o sufixo padrão aplicado quando faltar. */
    fun complete(typed: String): String {
        val digits = sanitize(typed)
        return if (digits.length < BASE_LEN) digits
        else digits + DEFAULT_SUFFIX.drop(digits.length - BASE_LEN)
    }

    /** O número só é válido para salvar se tiver pelo menos BASE_LEN dígitos. */
    fun isValid(typed: String): Boolean = sanitize(typed).length >= BASE_LEN

    override fun filter(text: AnnotatedString): TransformedText {
        val typed = text.text.take(TOTAL_LEN)
        val full = complete(typed)

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

/*
 * ===== Uso no campo de texto =====
 *
 * var lawsuitNumber by remember { mutableStateOf("") }
 *
 * OutlinedTextField(
 *     value = lawsuitNumber,
 *     onValueChange = { lawsuitNumber = ProcessNumberTransformation.sanitize(it) },
 *     visualTransformation = ProcessNumberTransformation,
 *     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
 *     isError = lawsuitNumber.isNotEmpty() && !ProcessNumberTransformation.isValid(lawsuitNumber),
 *     label = { Text("Número do processo") },
 *     modifier = Modifier.fillMaxWidth()
 * )
 *
 * TextButton(
 *     enabled = ProcessNumberTransformation.isValid(lawsuitNumber),
 *     onClick = {
 *         // salva o número COMPLETO (com sufixo), não o texto digitado
 *         val toSave = ProcessNumberTransformation.complete(lawsuitNumber)
 *         scope.launch(Dispatchers.IO) { repository.save(toSave) }
 *     }
 * ) { Text("Salvar") }
 *
 *
 * ===== Corrigir registros que já estão no banco (PostgreSQL) =====
 * Ajuste o nome da tabela. Só afeta números com 13 a 19 dígitos.
 *
 * UPDATE sua_tabela
 * SET lawsuit_number = lawsuit_number || substr('8100060', length(lawsuit_number) - 12)
 * WHERE length(lawsuit_number) BETWEEN 13 AND 19;
 */