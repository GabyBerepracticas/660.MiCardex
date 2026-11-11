package com.example.micardex.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.micardex.data.Etapa
import com.example.micardex.data.MINIMO_APROBATORIO
import com.example.micardex.data.Materia
import com.example.micardex.data.Perfil
import com.example.micardex.data.cerrada
import com.example.micardex.data.conParciales
import com.example.micardex.data.parciales

private fun calATexto(v: Double?): String =
    if (v == null) "" else if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

/** Datos básicos de la materia: nombre y semestre. */
@Composable
fun MateriaDialog(
    inicial: Materia,
    onCancelar: () -> Unit,
    onGuardar: (Materia) -> Unit
) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var semestre by remember { mutableStateOf(inicial.semestre.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                if (inicial.id == 0) "Nueva materia" else "Editar materia",
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre de la materia") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = semestre, onValueChange = { semestre = it },
                    label = { Text("Semestre") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                if (inicial.id == 0) {
                    Text(
                        "Después de guardar, toca \"Capturar\" en la tarjeta para registrar tus calificaciones parciales.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sem = semestre.trim().toIntOrNull()
                when {
                    nombre.isBlank() -> error = "Escribe el nombre de la materia."
                    sem == null || sem !in 1..15 -> error = "El semestre debe ser un número entre 1 y 15."
                    else -> onGuardar(inicial.copy(nombre = nombre.trim(), semestre = sem))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

/**
 * Captura de calificaciones parciales de UNA etapa (Normal, Repetición o Especial).
 * Promedio >= 70 -> aprobada en esta etapa. Si no, se puede marcar el curso como concluido
 * para pasar a la siguiente etapa.
 */
@Composable
fun ParcialesDialog(
    materia: Materia,
    etapa: Etapa,
    onCancelar: () -> Unit,
    onGuardar: (Materia) -> Unit
) {
    val campos = remember {
        mutableStateListOf<String>().apply {
            val previos = materia.parciales(etapa)
            if (previos.isEmpty()) repeat(3) { add("") } else previos.forEach { add(calATexto(it)) }
        }
    }
    var concluida by remember { mutableStateOf(materia.cerrada(etapa)) }
    var error by remember { mutableStateOf<String?>(null) }

    val numeros = campos.mapNotNull { it.trim().replace(',', '.').toDoubleOrNull() }
    val promedio = if (numeros.isEmpty()) 0.0 else Math.round(numeros.sum() / numeros.size * 100) / 100.0
    val aprobado = numeros.isNotEmpty() && promedio >= MINIMO_APROBATORIO

    val textoConcluir = when (etapa) {
        Etapa.NORMAL -> "Terminé este curso sin aprobar (pasar a Repetición)"
        Etapa.REPETICION -> "Terminé este curso sin aprobar (pasar a Especial)"
        Etapa.ESPECIAL -> "Terminé el Especial sin aprobar (materia reprobada)"
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Column {
                Text("Parciales · ${etapa.titulo}", color = MaterialTheme.colorScheme.primary)
                Text(materia.nombre, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary)
            }
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                campos.forEachIndexed { i, valor ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = if (i == 0) 0.dp else 8.dp)) {
                        OutlinedTextField(
                            value = valor, onValueChange = { campos[i] = it },
                            label = { Text("Parcial ${i + 1}") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        if (campos.size > 1) {
                            IconButton(onClick = { campos.removeAt(i) }) {
                                Icon(Icons.Default.Close, "Quitar parcial",
                                    tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                TextButton(onClick = { campos.add("") }) { Text("+ Agregar parcial") }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (aprobado) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Promedio: ${calATexto(promedio).ifEmpty { "0" }} / 100", fontWeight = FontWeight.Bold)
                        Text(
                            if (aprobado) "✓ Aprobada en ${etapa.corto} (mínimo ${MINIMO_APROBATORIO.toInt()})"
                            else "Aún no llega a ${MINIMO_APROBATORIO.toInt()} de promedio para aprobar",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (!aprobado) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)) {
                        Checkbox(checked = concluida, onCheckedChange = { concluida = it })
                        Text(textoConcluir, style = MaterialTheme.typography.bodySmall)
                    }
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val textos = campos.map { it.trim().replace(',', '.') }.filter { it.isNotEmpty() }
                val valores = textos.map { it.toDoubleOrNull() }
                when {
                    valores.any { it == null || it !in 0.0..100.0 } ->
                        error = "Cada parcial debe ser un número entre 0 y 100."
                    concluida && !aprobado && valores.isEmpty() ->
                        error = "Captura al menos un parcial para concluir el curso."
                    else -> onGuardar(
                        materia.conParciales(etapa, valores.filterNotNull(), concluida && !aprobado)
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun PerfilDialog(
    inicial: Perfil,
    obligatorio: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (Perfil) -> Unit
) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var control by remember { mutableStateOf(inicial.control) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!obligatorio) onCancelar() },
        properties = DialogProperties(
            dismissOnBackPress = !obligatorio,
            dismissOnClickOutside = !obligatorio
        ),
        title = {
            Text(
                if (obligatorio) "Bienvenido a Mi Cardex" else "Mis datos",
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                if (obligatorio) {
                    Text("Captura tus datos una sola vez; se guardarán en el dispositivo.",
                        modifier = Modifier.padding(bottom = 8.dp))
                }
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre del alumno") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = control, onValueChange = { control = it },
                    label = { Text("Número de control") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isBlank() || control.isBlank()) {
                    error = "Completa ambos campos."
                } else {
                    onGuardar(Perfil(nombre.trim(), control.trim()))
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            if (!obligatorio) TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
