package ui.components.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                append(c)
                if ((i == 1 || i == 3) && i != digits.lastIndex) append('/')
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, digits.length)
                val result = when {
                    safeOffset <= 1 -> safeOffset
                    safeOffset <= 3 -> safeOffset + 1
                    else -> safeOffset + 2
                }
                return result.coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                val result = when {
                    safeOffset <= 2 -> safeOffset
                    safeOffset <= 5 -> safeOffset - 1
                    else -> safeOffset - 2
                }
                return result.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
