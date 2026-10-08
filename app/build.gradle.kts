plugins {
    // ... tus plugins actuales (alias(libs.plugins.android.application), etc.)
    id("com.google.devtools.ksp") // Asegúrate de tener KSP habilitado para Room
}

dependencies {
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    
    // Jetpack Compose (suele venir por defecto en proyectos nuevos)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}

