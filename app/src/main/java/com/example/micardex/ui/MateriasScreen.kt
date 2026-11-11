package com.example.micardex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.micardex.data.Etapa
import com.example.micardex.data.Materia
import com.example.micardex.data.aprobada
import com.example.micardex.data.cerrada
import com.example.micardex.data.estado
import com.example.micardex.data.estatusTexto
import com.example.micardex.data.parciales
import com.example.micardex.data.promedio

/** Formatea una calificación: 85.0 -> "85", 87.5 -> "87.5", null -> "—". */
fun fmtCal(valor: Double?): String = when {
    valor == null -> "—"
    valor % 1.0 == 0.0 -> valor.toInt().toString()
    else -> String.format("%.2f", valor).trimEnd('0').trimEnd('.')
}

@Composable
fun MateriasScreen(
    materias: List<Materia>,
    onEditar: (Materia) -> Unit,
    onParciales: (Materia, Etapa) -> Unit,
    onEliminar: (Materia) -> Unit
) {
    var porEliminar by remember { mutableStateOf<Materia?>(null) }

    if (materias.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Aún no tienes materias.\nToca + para agregar la primera.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(32.dp)
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            materias.groupBy { it.semestre }.toSortedMap().forEach { (semestre, lista) ->
                item(key = "sem_$semestre") {
                    Text(
                        "Semestre $semestre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                items(lista, key = { it.id }) { materia ->
                    MateriaCard(
                        materia = materia,
                        onEditar = { onEditar(materia) },
                        onParciales = { etapa -> onParciales(materia, etapa) },
                        onEliminar = { porEliminar = materia }
                    )
                }
            }
        }
    }

    porEliminar?.let { m ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar materia") },
            text = { Text("¿Seguro que quieres eliminar \"${m.nombre}\"?") },
            confirmButton = {
                TextButton(onClick = { onEliminar(m); porEliminar = null }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun MateriaCard(
    materia: Materia,
    onEditar: () -> Unit,
    onParciales: (Etapa) -> Unit,
    onEliminar: () -> Unit
) {
    val estado = materia.estado()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    materia.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEditar) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (estado.aprobada) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    materia.estatusTexto(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (estado.aprobada) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // Se muestran solo las etapas por las que ya pasó la materia.
            Etapa.values().filter { it.ordinal <= estado.etapa.ordinal }.forEach { etapa ->
                val parciales = materia.parciales(etapa)
                val resultado = when {
                    materia.aprobada(etapa) -> "Aprobada"
                    materia.cerrada(etapa) -> "No aprobada"
                    else -> "En curso"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(etapa.corto, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary)
                        Text(
                            if (parciales.isEmpty()) "Sin parciales capturados"
                            else "${fmtCal(materia.promedio(etapa))} de promedio · ${parciales.size} parciales · $resultado",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = { onParciales(etapa) }) {
                        Text(if (parciales.isEmpty()) "Capturar" else "Editar")
                    }
                }
            }

            if (materia.calFinal != null) {
                Text(
                    "Calificación final: ${fmtCal(materia.calFinal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
