package com.tuapp.gymlocal // Cambia esto por tu paquete real

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entidad (La tabla en la base de datos)
@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val weight: Float,
    val reps: Int,
    val date: String
)

// 2. DAO (Operaciones de base de datos)
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_table ORDER BY id DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Insert
    suspend fun insertWorkout(workout: Workout)
}

// 3. Base de Datos Singleton
@Database(entities = [Workout::class], version = 1, exportSchema = false)
abstract class GymDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: GymDatabase? = null

        fun getDatabase(context: Context): GymDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GymDatabase::class.java,
                    "gym_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
