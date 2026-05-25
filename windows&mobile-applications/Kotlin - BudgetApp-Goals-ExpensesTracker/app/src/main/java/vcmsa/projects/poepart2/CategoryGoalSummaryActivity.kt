package vcmsa.projects.poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class CategoryGoalSummaryActivity : AppCompatActivity() {

    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = Firebase.auth.currentUser
        if (user == null) {
            // Not signed in → send them back to the login screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        val uid = user.uid

        setContentView(R.layout.activity_goal_summary)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val percentText = findViewById<TextView>(R.id.percentText)
        val rawText = findViewById<TextView>(R.id.rawText)

        // Determine this month’s key and date range
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthKey = monthFormat.format(Date())             // e.g. "2025-06"
        val startOfMonth = "$monthKey-01"
        // Last day:
        val cal = Calendar.getInstance().apply {
            time = monthFormat.parse(monthKey)!!
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val endOfMonth = sdfDay.format(cal.time)

        // Fetch this month’s goals
        db.collection("users").document(uid)
            .collection("budgetGoals")
            .document(monthKey)
            .get()
            .addOnSuccessListener { goalDoc ->
                val minBudget = (goalDoc.getDouble("minBudget") ?: 0.0)
                val maxBudget = (goalDoc.getDouble("maxBudget") ?: 0.0)

                // Fetch all expenses in this month
                db.collection("users").document(uid)
                    .collection("expenses")
                    .whereGreaterThanOrEqualTo("date", startOfMonth)
                    .whereLessThanOrEqualTo("date", endOfMonth)
                    .get()
                    .addOnSuccessListener { snaps ->
                        // Sum up amounts
                        val totalSpent = snaps.documents
                            .sumOf { it.getDouble("amount") ?: 0.0 }

                        // Calculate percent of max goal (clamp 0–100)
                        val percent = if (maxBudget > 0) {
                            ((totalSpent / maxBudget) * 100).toInt().coerceIn(0, 100)
                        } else 0

                        // Update UI
                        progressBar.progress = percent
                        percentText.text = "$percent% of max goal"
                        rawText.text = "Spent: \$${"%.2f".format(totalSpent)} / \$${"%.2f".format(maxBudget)}"
                    }
            }
    }
}
