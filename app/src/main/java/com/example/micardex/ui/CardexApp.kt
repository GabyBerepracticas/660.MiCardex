package com.example.micardex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.micardex.CardexViewModel
import com.example.micardex.data.Etapa
import com.example.micardex.data.Materia

@Composable
fun CardexApp(vm: CardexViewModel = viewModel()) {
    val perfil by vm.perfil.collectAsStateWithLifecycle()
    val materias by vm.materias.collectAsStateWithLifecycle()
    val general by vm.promedioGeneral.collectAsStateWithLifecycle()
    val porSemestre by vm.promediosSemestre.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var formMateria by remember { mutableStateOf<Materia?>(null) } // null = cerrado
    var parciales by remember { mutableStateOf<Pair<Materia, Etapa>?>(null) } // null = cerrado
    var editarPerfil by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { Encabezado(nombre = perfil.nombre, control = perfil.control) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val colores = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 }, colors = colores,
                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                    label = { Text("Materias") }
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 }, colors = colores,
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Promedios") }
                )
                NavigationBarItem(
                    selected = tab == 2, onClick = { tab = 2 }, colors = colores,
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(
                    onClick = { formMateria = Materia() },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, contentDescription = "Agregar materia") }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> MateriasScreen(
                    materias = materias,
                    onEditar = { formMateria = it },
                    onParciales = { m, etapa -> parciales = m to etapa },
                    onEliminar = { vm.eliminarMateria(it) }
                )
                1 -> PromediosScreen(general = general, porSemestre = porSemestre)
                else -> PerfilScreen(perfil = perfil, onEditar = { editarPerfil = true })
            }
        }
    }

    formMateria?.let { materia ->
        MateriaDialog(
            inicial = materia,
            onCancelar = { formMateria = null },
            onGuardar = {
                vm.guardarMateria(it)
                formMateria = null
            }
        )
    }

    parciales?.let { (materia, etapa) ->
        ParcialesDialog(
            materia = materia,
            etapa = etapa,
            onCancelar = { parciales = null },
            onGuardar = {
                vm.guardarMateria(it)
                parciales = null
            }
        )
    }

    // La primera vez (perfil vacío) el diálogo es obligatorio.
    if (!perfil.completo || editarPerfil) {
        PerfilDialog(
            inicial = perfil,
            obligatorio = !perfil.completo,
            onCancelar = { editarPerfil = false },
            onGuardar = {
                vm.guardarPerfil(it)
                editarPerfil = false
            }
        )
    }
}

@Composable
private fun Encabezado(nombre: String, control: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(GradienteCardex)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Mi Cardex", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        if (nombre.isNotBlank()) {
            Text(nombre, color = Color.White, fontSize = 15.sp)
            Text("No. de control: $control", color = Color(0xFFBBDEFB), fontSize = 13.sp)
        }
    }
}
