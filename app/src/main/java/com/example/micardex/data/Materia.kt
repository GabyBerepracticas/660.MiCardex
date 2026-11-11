package com.example.micardex.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Promedio mínimo de los parciales para aprobar una etapa. */
const val MINIMO_APROBATORIO = 70.0

/** Etapas por las que puede pasar una materia, en orden. */
enum class Etapa(val titulo: String, val corto: String, val siguiente: String) {
    NORMAL("Curso Normal", "Normal", "Repetición"),
    REPETICION("Curso en Repetición", "Repetición", "Especial"),
    ESPECIAL("Curso Especial", "Especial", "")
}

/**
 * Única tabla de la base de datos: "materias".
 *
 * - parcialesXxx: calificaciones parciales de cada etapa, guardadas como texto "20,18.5,25".
 * - cerradoXxx: el alumno indicó que ese curso terminó sin aprobar (pasa a la siguiente etapa).
 * - calNormal / calRepeticion / calEspecial: promedio de los parciales de cada etapa.
 * - calFinal: calificación definitiva (solo existe cuando la materia ya aprobó o agotó etapas).
 */
@Entity(tableName = "materias")
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String = "",
    val semestre: Int = 1,
    val calNormal: Double? = null,
    val calRepeticion: Double? = null,
    val calEspecial: Double? = null,
    @ColumnInfo(defaultValue = "''") val parcialesNormal: String = "",
    @ColumnInfo(defaultValue = "''") val parcialesRepeticion: String = "",
    @ColumnInfo(defaultValue = "''") val parcialesEspecial: String = "",
    @ColumnInfo(defaultValue = "0") val cerradoNormal: Boolean = false,
    @ColumnInfo(defaultValue = "0") val cerradoRepeticion: Boolean = false,
    @ColumnInfo(defaultValue = "0") val cerradoEspecial: Boolean = false,
    val calFinal: Double? = null
)

/** Resultado de la consulta de promedio por semestre. */
data class PromedioSemestre(
    val semestre: Int,
    val promedio: Double
)

/** Dónde se encuentra la materia: etapa actual, si aprobó y si ya terminó todo el proceso. */
data class EstadoMateria(val etapa: Etapa, val aprobada: Boolean, val concluida: Boolean)

// ---------------------------------------------------------------- lógica de etapas

private fun parsear(texto: String): List<Double> =
    texto.split(",").mapNotNull { it.trim().toDoubleOrNull() }

fun Materia.parciales(e: Etapa): List<Double> = parsear(
    when (e) {
        Etapa.NORMAL -> parcialesNormal
        Etapa.REPETICION -> parcialesRepeticion
        Etapa.ESPECIAL -> parcialesEspecial
    }
)

/** Promedio de los parciales de la etapa (suma de parciales / número de parciales). */
fun Materia.promedio(e: Etapa): Double {
    val p = parciales(e)
    return if (p.isEmpty()) 0.0 else Math.round(p.sum() / p.size * 100) / 100.0
}

fun Materia.cerrada(e: Etapa): Boolean = when (e) {
    Etapa.NORMAL -> cerradoNormal
    Etapa.REPETICION -> cerradoRepeticion
    Etapa.ESPECIAL -> cerradoEspecial
}

fun Materia.aprobada(e: Etapa): Boolean =
    parciales(e).isNotEmpty() && promedio(e) >= MINIMO_APROBATORIO

/**
 * Regla principal: si en una etapa el promedio de parciales llega a 70 o más, la materia queda
 * aprobada ahí y NO pasa a la siguiente. Si no aprueba y el curso se marcó como concluido,
 * pasa a la siguiente etapa (Normal -> Repetición -> Especial).
 */
fun Materia.estado(): EstadoMateria {
    for (e in Etapa.values()) {
        if (aprobada(e)) return EstadoMateria(e, aprobada = true, concluida = true)
        if (!cerrada(e)) return EstadoMateria(e, aprobada = false, concluida = false)
    }
    return EstadoMateria(Etapa.ESPECIAL, aprobada = false, concluida = true) // reprobada
}

private fun Materia.sinEtapa(e: Etapa): Materia = when (e) {
    Etapa.NORMAL -> copy(parcialesNormal = "", cerradoNormal = false)
    Etapa.REPETICION -> copy(parcialesRepeticion = "", cerradoRepeticion = false)
    Etapa.ESPECIAL -> copy(parcialesEspecial = "", cerradoEspecial = false)
}

private fun Materia.promedioONull(e: Etapa): Double? =
    if (parciales(e).isEmpty()) null else promedio(e)

/** Limpia etapas que ya no aplican y recalcula promedios y calificación final. */
fun Materia.normalizada(): Materia {
    val est = estado()
    var m = this
    Etapa.values().filter { it.ordinal > est.etapa.ordinal }.forEach { m = m.sinEtapa(it) }
    return m.copy(
        calNormal = m.promedioONull(Etapa.NORMAL),
        calRepeticion = m.promedioONull(Etapa.REPETICION),
        calEspecial = m.promedioONull(Etapa.ESPECIAL),
        calFinal = if (est.concluida) m.promedio(est.etapa) else null
    )
}

/** Guarda los parciales de una etapa y vuelve a calcular todo. */
fun Materia.conParciales(etapa: Etapa, lista: List<Double>, concluida: Boolean): Materia {
    val texto = lista.joinToString(",")
    val m = when (etapa) {
        Etapa.NORMAL -> copy(parcialesNormal = texto, cerradoNormal = concluida)
        Etapa.REPETICION -> copy(parcialesRepeticion = texto, cerradoRepeticion = concluida)
        Etapa.ESPECIAL -> copy(parcialesEspecial = texto, cerradoEspecial = concluida)
    }
    return m.normalizada()
}

fun Materia.estatusTexto(): String {
    val s = estado()
    return when {
        s.aprobada -> "Aprobada en ${s.etapa.corto}"
        s.concluida -> "Reprobada (agotó Especial)"
        else -> "En curso · ${s.etapa.corto}"
    }
}
