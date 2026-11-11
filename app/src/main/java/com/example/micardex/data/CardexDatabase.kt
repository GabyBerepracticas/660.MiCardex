package com.example.micardex.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Materia::class], version = 2, exportSchema = false)
abstract class CardexDatabase : RoomDatabase() {

    abstract fun materiaDao(): MateriaDao

    companion object {
        @Volatile
        private var INSTANCE: CardexDatabase? = null

        /** v1 -> v2: agrega parciales por etapa y conserva las calificaciones ya capturadas. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE materias ADD COLUMN parcialesNormal TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE materias ADD COLUMN parcialesRepeticion TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE materias ADD COLUMN parcialesEspecial TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE materias ADD COLUMN cerradoNormal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE materias ADD COLUMN cerradoRepeticion INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE materias ADD COLUMN cerradoEspecial INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE materias ADD COLUMN calFinal REAL")

                // Las calificaciones antiguas pasan a ser un único "parcial" de su etapa.
                db.execSQL("UPDATE materias SET parcialesNormal = CAST(calNormal AS TEXT) WHERE calNormal IS NOT NULL")
                db.execSQL("UPDATE materias SET parcialesRepeticion = CAST(calRepeticion AS TEXT) WHERE calRepeticion IS NOT NULL")
                db.execSQL("UPDATE materias SET parcialesEspecial = CAST(calEspecial AS TEXT) WHERE calEspecial IS NOT NULL")
                db.execSQL("UPDATE materias SET cerradoNormal = 1 WHERE calRepeticion IS NOT NULL OR calEspecial IS NOT NULL")
                db.execSQL("UPDATE materias SET cerradoRepeticion = 1 WHERE calEspecial IS NOT NULL")
                db.execSQL("UPDATE materias SET calFinal = COALESCE(calEspecial, calRepeticion, calNormal)")
                db.execSQL("UPDATE materias SET calFinal = NULL WHERE calFinal < 70 AND calEspecial IS NULL")
            }
        }

        fun get(context: Context): CardexDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CardexDatabase::class.java,
                    "cardex.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
