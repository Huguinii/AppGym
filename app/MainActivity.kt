package com.tuapp.gymlocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = GymDatabase.getDatabase(applicationContext)
        val dao = database.workoutDao()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GymViewModel = viewModel(
                        factory = GymViewModelFactory(dao)
                    )
                    GymScreen(viewModel)
                }
            }
        }
    }
}

// ViewModel para conectar la UI con la Base de Datos
class GymViewModel(private val dao: WorkoutDao) : ViewModel() {
    val workouts = dao.getAllWorkouts().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addWorkout(name: String, weight: String, reps: String) {
        val weightFloat = weight.toFloatOrNull() ?: 0f
        val repsInt = reps.toIntOrNull() ?: 0
        if (name.isNotBlank()) {
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            viewModelScope.launch {
                dao.insertWorkout(Workout(exerciseName = name, weight = weightFloat, reps = repsInt, date = currentDate))
            }
        }
    }
}

class GymViewModelFactory(private val dao: WorkoutDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GymViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Pantalla Principal en Jetpack Compose
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymScreen(viewModel: GymViewModel) {
    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    val workoutList by viewModel.workouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Diario de Gimnasio (Local)") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Formulario de Entrada
            OutlinedTextField(
                value = exerciseName,
                onValueChange = { exerciseName = it },
                label = { Text("Nombre del Ejercicio (ej. Press de Banca)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Repeticiones") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addWorkout(exerciseName, weight, reps)
                    exerciseName = ""
                    weight = ""
                    reps = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Serie")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Historial de Entrenamientos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Historial Local
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workoutList) { workout ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = workout.exerciseName, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Peso: ${workout.weight} kg | Reps: ${workout.reps}")
                            Text(text = "Fecha: ${workout.date}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
