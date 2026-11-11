package com.example.micardex.data

import android.content.Context

data class Perfil(val nombre: String = "", val control: String = "") {
    val completo: Boolean get() = nombre.isNotBlank() && control.isNotBlank()
}

/**
 * Guarda de forma permanente el nombre y el número de control del alumno
 * usando SharedPreferences (NO van dentro de la base de datos Room).
 */
class PerfilStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("perfil_alumno", Context.MODE_PRIVATE)

    fun leer(): Perfil = Perfil(
        nombre = prefs.getString(KEY_NOMBRE, "") ?: "",
        control = prefs.getString(KEY_CONTROL, "") ?: ""
    )

    fun guardar(perfil: Perfil) {
        prefs.edit()
            .putString(KEY_NOMBRE, perfil.nombre.trim())
            .putString(KEY_CONTROL, perfil.control.trim())
            .apply()
    }

    private companion object {
        const val KEY_NOMBRE = "nombre"
        const val KEY_CONTROL = "numero_control"
    }
}
