package vcmsa.projects.poepart2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CategoryGraphActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val uid = Firebase.auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_graph)

        val chart = findViewById<BarChart>(R.id.barChart)

        // TODO: replace these with your actual period bounds
        val startOfPeriod = "2025-06-01"
        val endOfPeriod   = "2025-06-30"

        // fetch goals
        db.collection("users")
            .document(uid)
            .collection("budgetGoals")
            .document("2025-06")  // your month‐key logic
            .get()
            .addOnSuccessListener { goalDoc ->
                val minGoal = (goalDoc.getDouble("minBudget") ?: 0.0).toFloat()
                val maxGoal = (goalDoc.getDouble("maxBudget") ?: 0.0).toFloat()

                // fetch expenses in period
                db.collection("users")
                    .document(uid)
                    .collection("expenses")
                    .whereGreaterThanOrEqualTo("date", startOfPeriod)
                    .whereLessThanOrEqualTo("date", endOfPeriod)
                    .get()
                    .addOnSuccessListener { snaps ->
                        // sum by categoryId
                        val sums = snaps.documents
                            .groupBy { it.getString("categoryId") ?: "" }
                            .mapValues { it.value.sumOf { doc -> (doc.getDouble("amount") ?: 0.0) } }

                        // build entries
                        val entries = sums.entries.mapIndexed { i, (_, total) ->
                            BarEntry(i.toFloat(), total.toFloat())
                        }

                        // render chart
                        val dataSet = BarDataSet(entries, "Spent per Category")
                        chart.data = BarData(dataSet)

                        // add goal lines
                        chart.axisLeft.apply {
                            addLimitLine(LimitLine(minGoal, "Min Goal"))
                            addLimitLine(LimitLine(maxGoal, "Max Goal"))
                        }

                        chart.invalidate()
                    }
            }
    }
}
