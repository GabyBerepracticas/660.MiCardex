package com.example.micardex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.micardex.data.CardexDatabase
import com.example.micardex.data.Materia
import com.example.micardex.data.Perfil
import com.example.micardex.data.PerfilStore
import com.example.micardex.data.PromedioSemestre
import com.example.micardex.data.normalizada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardexViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = CardexDatabase.get(app).materiaDao()
    private val store = PerfilStore(app)

    val materias: StateFlow<List<Materia>> = dao.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promedioGeneral: StateFlow<Double?> = dao.observarPromedioGeneral()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val promediosSemestre: StateFlow<List<PromedioSemestre>> = dao.observarPromediosPorSemestre()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _perfil = MutableStateFlow(store.leer())
    val perfil: StateFlow<Perfil> = _perfil.asStateFlow()

    init {
        // Recalcula los registros existentes con la regla de promedio (por si se capturaron con la versión anterior).
        viewModelScope.launch {
            dao.observarTodas().first().forEach { m ->
                val n = m.normalizada()
                if (n != m) dao.guardar(n)
            }
        }
    }

    fun guardarPerfil(perfil: Perfil) {
        store.guardar(perfil)
        _perfil.value = store.leer()
    }

    fun guardarMateria(materia: Materia) {
        viewModelScope.launch { dao.guardar(materia) }
    }

    fun eliminarMateria(materia: Materia) {
        viewModelScope.launch { dao.eliminar(materia) }
    }
}
