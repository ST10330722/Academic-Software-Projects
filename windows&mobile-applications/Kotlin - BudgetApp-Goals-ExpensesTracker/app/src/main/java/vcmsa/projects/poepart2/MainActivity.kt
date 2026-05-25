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
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure Firebase SDK is initialized
        auth = Firebase.auth

        setContentView(R.layout.loginpage)

        val emailInput   = findViewById<EditText>(R.id.usernameInput)
        val passwordInput= findViewById<EditText>(R.id.passwordInput)
        val loginButton  = findViewById<Button>(R.id.loginButton)
        val signupText   = findViewById<TextView>(R.id.signupText)

        loginButton.setOnClickListener {
            val email    = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Move on to HomeActivity
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }
    }
}
