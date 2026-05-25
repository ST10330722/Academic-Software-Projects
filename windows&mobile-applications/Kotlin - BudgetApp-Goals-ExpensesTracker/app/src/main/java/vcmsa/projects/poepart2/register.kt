package vcmsa.projects.poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth

        setContentView(R.layout.activity_register)

        val nameInput         = findViewById<EditText>(R.id.nameInput)
        val emailInput        = findViewById<EditText>(R.id.emailInput)
        val passwordInput     = findViewById<EditText>(R.id.passwordInput)
        val registerButton    = findViewById<Button>(R.id.registerButton)
        val loginRedirectText = findViewById<TextView>(R.id.loginRedirectText)

        registerButton.setOnClickListener {
            val fullName = nameInput.text.toString().trim()
            val email    = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Save the user's display name in Firestore
                        val uid = auth.currentUser!!.uid
                        val profile = mapOf("fullName" to fullName)
                        Firebase.firestore
                            .collection("users")
                            .document(uid)
                            .set(profile)

                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        loginRedirectText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
