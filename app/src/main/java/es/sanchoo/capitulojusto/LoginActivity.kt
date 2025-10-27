package es.sanchoo.capitulojusto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Cambiar el color de la barra de estado (status bar)
        window.statusBarColor = getColor(R.color.red)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMainActivity()
            return
        }

        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Establecer valores por defecto
        val defaultValues = hashMapOf<String, Any>(
            "sePermitenNuevosUsuarios" to true
        )
        remoteConfig.setDefaultsAsync(defaultValues)

        // Obtener valores de Remote Config
        fetchRemoteConfig()


        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        progressBar = findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            loginUser()
        }

        registerButton.setOnClickListener {
            val sePermitenNuevosUsuarios = remoteConfig.getBoolean("sePermitenNuevosUsuarios")
            if (sePermitenNuevosUsuarios) {
                registerUser()
            } else {
                Toast.makeText(
                    this,
                    "Lo sentimos, actualmente no se permiten nuevos registros",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fetchRemoteConfig() {
        Log.d(TAG, "Iniciando fetchRemoteConfig...")
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Remote Config actualizado exitosamente desde Firebase")
                    val valor = remoteConfig.getBoolean("sePermitenNuevosUsuarios")
                    Log.d(TAG, "Valor de sePermitenNuevosUsuarios: $valor")
                    updateRegisterButtonState()
                } else {
                    Log.e(TAG, "Error al actualizar Remote Config: ${task.exception?.message}")
                    Log.d(TAG, "Usando valores por defecto")
                    updateRegisterButtonState()
                }
            }
    }

    private fun updateRegisterButtonState() {
        val sePermitenNuevosUsuarios = remoteConfig.getBoolean("sePermitenNuevosUsuarios")
        Log.d(TAG, "updateRegisterButtonState - sePermitenNuevosUsuarios: $sePermitenNuevosUsuarios")
        registerButton.isEnabled = sePermitenNuevosUsuarios
        registerButton.alpha = if (sePermitenNuevosUsuarios) 1.0f else 0.5f

        Toast.makeText(
            this,
            "Remote Config cargado: nuevos usuarios ${if (sePermitenNuevosUsuarios) "permitidos" else "NO permitidos"}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!validateInput(email, password)) {
            return
        }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error al iniciar sesión: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (!validateInput(email, password)) {
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error al registrarse: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEditText.error = "Ingrese un correo electrónico"
            emailEditText.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Ingrese un correo electrónico válido"
            emailEditText.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Ingrese una contraseña"
            passwordEditText.requestFocus()
            return false
        }

        if (password.length < 6) {
            passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
            passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
        val sePermitenNuevosUsuarios = remoteConfig.getBoolean("sePermitenNuevosUsuarios")
        registerButton.isEnabled = !show && sePermitenNuevosUsuarios
        emailEditText.isEnabled = !show
        passwordEditText.isEnabled = !show
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

