package ui.components.displays

import androidx.compose.ui.graphics.Color
import ui.theme.MemoraColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun dueInfo(dutyDate: LocalDate?): Pair<String, Color> {
    if (dutyDate == null) return "Sem vencimento" to MemoraColors.Neutral
    val days = ChronoUnit.DAYS.between(LocalDate.now(), dutyDate)
    return when {
        days < 0L -> "Vencido há ${-days} ${if (-days == 1L) "dia" else "dias"}" to MemoraColors.Danger
        days == 0L -> "Vence hoje" to MemoraColors.Danger
        days <= 30L -> "Vence em $days ${if (days == 1L) "dia" else "dias"}" to MemoraColors.Warning
        else -> "Vence em $days dias" to MemoraColors.Success
    }
}

fun pendingInfo(administrativePending: String?): Pair<String, Color> =
    when (administrativePending) {
        null, "", "SEM PENDÊNCIA" -> "SEM PENDÊNCIA" to MemoraColors.Success
        "DOCUMENTO", "PEDIDO" -> administrativePending.orEmpty() to MemoraColors.Warning
        "PARECER", "DECISÃO" -> administrativePending.orEmpty() to MemoraColors.Danger
        else -> (administrativePending ?: "SEM PENDÊNCIA") to MemoraColors.Neutral
    }

fun sentenceTypeLabel(type: Char): String = when (type.uppercaseChar()) {
    'S' -> "Progressão ao semiaberto"
    'A' -> "Progressão ao aberto"
    'L' -> "Livramento condicional"
    else -> "Não informado"
}

fun jailLabel(jail: Char): String = when (jail.uppercaseChar()) {
    'A' -> "APAC"
    'U' -> "UPTIM"
    'R' -> "Regime Aberto"
    else -> "Não informado"
}
