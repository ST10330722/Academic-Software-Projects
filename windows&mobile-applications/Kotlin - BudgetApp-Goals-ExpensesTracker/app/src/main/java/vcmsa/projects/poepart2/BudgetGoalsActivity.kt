package vcmsa.projects.poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class BudgetGoalsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Initialize Auth and check for signed-in user
        auth = Firebase.auth
        val user = auth.currentUser
        if (user == null) {
            // not signed in → back to login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        val uid = user.uid

        // 2) Inflate your layout
        setContentView(R.layout.activity_budget_goals)

        // 3) Bind views
        val minInput = findViewById<EditText>(R.id.minBudgetInput)
        val maxInput = findViewById<EditText>(R.id.maxBudgetInput)
        val saveBtn  = findViewById<Button>(R.id.saveGoalsButton)

        // 4) Pre-fill if goals already exist for this month
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        db.collection("users")
            .document(uid)
            .collection("budgetGoals")
            .document(monthKey)
            .get()
            .addOnSuccessListener { doc ->
                doc.getDouble("minBudget")?.let {
                    minInput.setText(it.toString())
                }
                doc.getDouble("maxBudget")?.let {
                    maxInput.setText(it.toString())
                }
            }

        // 5) Save button logic
        saveBtn.setOnClickListener {
            val minText = minInput.text.toString().trim()
            val maxText = maxInput.text.toString().trim()

            val minVal = minText.toDoubleOrNull()
            val maxVal = maxText.toDoubleOrNull()

            if (minVal == null || maxVal == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Write to Firestore
            val payload = mapOf(
                "minBudget" to minVal,
                "maxBudget" to maxVal
            )
            db.collection("users")
                .document(uid)
                .collection("budgetGoals")
                .document(monthKey)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Budget goals saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Save failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
