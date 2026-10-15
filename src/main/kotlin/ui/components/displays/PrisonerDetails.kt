package ui.components.displays

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import backend.model.Prisoner
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun LocalDate?.formatOrDash(): String = this?.format(dateFormatter) ?: "—"

private fun sentenceTypeLabel(type: Char): String = when (type.uppercaseChar()) {
    'S' -> "Progressão ao semiaberto"
    'A' -> "Progressão ao aberto"
    'L' -> "Livramento condicional"
    else -> "Não informado"
}

private fun jailLabel(jail: Char): String = when (jail.uppercaseChar()) {
    'A' -> "APAC"
    'U' -> "UPTIM"
    else -> "Não informado"
}

/** Dias para o vencimento = dutyDate - hoje, com texto e cor. */
private fun dueInfo(dutyDate: LocalDate?): Pair<String, Color> {
    if (dutyDate == null) return "Sem data de vencimento" to Color.Gray
    val days = ChronoUnit.DAYS.between(LocalDate.now(), dutyDate)
    return when {
        days < 0L -> "Vencido há ${-days} ${if (-days == 1L) "dia" else "dias"}" to Color(0xFFC62828)
        days == 0L -> "Vence hoje" to Color(0xFFC62828)
        days <= 30L -> "Vence em $days ${if (days == 1L) "dia" else "dias"}" to Color(0xFFEF6C00)
        else -> "Vence em $days dias" to Color(0xFF2E7D32)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        Text(
            value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PrisonerDetails(prisoner: Prisoner, modifier: Modifier = Modifier) {
    val (dueText, dueColor) = dueInfo(prisoner.dutyDate())

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identificação
        Column {
            Text(
                prisoner.prisonerName(),
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Processo ${prisoner.lawsuitNum()}",
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }

        // Destaque: dias para o vencimento
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(dueColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text("Vencimento", style = MaterialTheme.typography.caption, color = dueColor)
                Text(
                    dueText,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = dueColor
                )
            }
        }

        // Informações
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                DetailRow("Tipo de pena", sentenceTypeLabel(prisoner.sentenceType()))
                Divider()
                DetailRow("Unidade", jailLabel(prisoner.jail()))
                Divider()
                DetailRow("Última análise", prisoner.dateLastAnalysis().formatOrDash())
                Divider()
                DetailRow("Último atendimento", prisoner.dateLastService().formatOrDash())
                Divider()
                DetailRow("Transferência APAC", prisoner.dateTransferApac().formatOrDash())
            }
        }
    }
}