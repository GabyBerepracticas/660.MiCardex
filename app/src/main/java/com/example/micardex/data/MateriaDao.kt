package com.example.micardex.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MateriaDao {

    /** Inserta si id = 0 (nueva) o actualiza si ya existe. */
    @Upsert
    suspend fun guardar(materia: Materia)

    @Delete
    suspend fun eliminar(materia: Materia)

    @Query("SELECT * FROM materias ORDER BY semestre ASC, nombre ASC")
    fun observarTodas(): Flow<List<Materia>>

    /** Promedio general de las calificaciones definitivas (materias ya concluidas). */
    @Query("SELECT AVG(calFinal) FROM materias WHERE calFinal IS NOT NULL")
    fun observarPromedioGeneral(): Flow<Double?>

    /** Promedio agrupado por semestre (solo materias concluidas). */
    @Query(
        """
        SELECT semestre, AVG(calFinal) AS promedio
        FROM materias
        WHERE calFinal IS NOT NULL
        GROUP BY semestre
        ORDER BY semestre ASC
        """
    )
    fun observarPromediosPorSemestre(): Flow<List<PromedioSemestre>>
}
