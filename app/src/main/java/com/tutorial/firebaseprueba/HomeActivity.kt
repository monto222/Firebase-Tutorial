package com.tutorial.firebaseprueba

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.remoteConfig
import com.tutorial.firebaseprueba.databinding.ActivityHomeBinding

enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}

private lateinit var binding: ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardamos los datos del usuario que se ha autenticado

        val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        //Remote config
        binding.errorButton.visibility = View.INVISIBLE
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val showErrorButton = Firebase.remoteConfig.getBoolean("show_error_button")
                val errorButtonText = Firebase.remoteConfig.getString("error_button_text")

                if (showErrorButton) {
                    binding.errorButton.visibility = View.VISIBLE
                }
                binding.errorButton.text = errorButtonText
            }
        }

    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        binding.emailTextView.text = email
        binding.providerTextView.text = provider

        binding.logOutButton.setOnClickListener {

            // Borrado de datos
            val prefs =
                getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()

            // Cerrar la actividad actual
            finish()
        }


        binding.errorButton.setOnClickListener {

            FirebaseCrashlytics.getInstance().setUserId(email)
            FirebaseCrashlytics.getInstance().setCustomKey("provider", provider)

            throw RuntimeException("Forzado de error")
        }

        binding.saveButton.setOnClickListener {
            db.collection("users").document(email).set(
                hashMapOf(
                    "provider" to provider,
                    "address" to binding.addressTextView.text.toString(),
                    "phone" to binding.phoneTextView.text.toString()
                )
            )
        }
        binding.getButton.setOnClickListener {
            db.collection("users").document(email).get().addOnSuccessListener {
                binding.addressTextView.setText(it.get("address") as String?)
                binding.phoneTextView.setText(it.get("phone") as String?)
            }
        }
        binding.deleteButton.setOnClickListener {
            db.collection("users").document(email).delete()
        }

    }
}