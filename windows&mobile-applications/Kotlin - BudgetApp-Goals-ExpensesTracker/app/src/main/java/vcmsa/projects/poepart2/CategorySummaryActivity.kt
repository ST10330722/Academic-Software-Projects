package vcmsa.projects.poepart2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.data.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CategorySummaryActivity : AppCompatActivity() {
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var calculateBtn: Button
    private lateinit var categoryListView: ListView


    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")


    private val db by lazy { AppDatabase.getInstance(this) }
    private val expenseDao by lazy { db.expenseDao() }
    private val categoryDao by lazy { db.categoryDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_summary)

        startDateInput   = findViewById(R.id.startDateInput)
        endDateInput     = findViewById(R.id.endDateInput)
        calculateBtn     = findViewById(R.id.calculateBtn)
        categoryListView = findViewById(R.id.categoryListView)


        listOf(startDateInput, endDateInput).forEach { field ->
            field.setOnClickListener { showDatePicker(field) }
        }

        calculateBtn.setOnClickListener { calculateTotals() }
    }

    private fun showDatePicker(target: EditText) {
        val today = LocalDate.now()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = LocalDate.of(year, month + 1, day)
                target.setText(date.format(fmt))
            },
            today.year, today.monthValue - 1, today.dayOfMonth
        ).show()
    }

    private fun calculateTotals() {
        val startText = startDateInput.text.toString().trim()
        val endText   = endDateInput.text.toString().trim()
        if (startText.isEmpty() || endText.isEmpty()) {
            Toast.makeText(this, "Enter both dates", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val startDate = LocalDate.parse(startText, fmt)
            val endDate   = LocalDate.parse(endText, fmt)

            lifecycleScope.launch(Dispatchers.IO) {

                val allExpenses  = expenseDao.getAllExpenses().first()
                val allCategories= categoryDao.getAllCategories().first()


                val totals = allExpenses
                    .filter { exp ->
                        val d = LocalDate.parse(exp.date, fmt)
                        !d.isBefore(startDate) && !d.isAfter(endDate)
                    }
                    .groupBy { it.categoryId }
                    .mapKeys { (catId, _) ->

                        allCategories.firstOrNull { it.id == catId }?.name ?: "Unknown"
                    }
                    .mapValues { entry ->
                        entry.value.sumOf { it.amount }
                    }


                val summary = totals.map { (catName, sum) ->
                    "$catName: R${"%.2f".format(sum)}"
                }

                withContext(Dispatchers.Main) {
                    categoryListView.adapter = ArrayAdapter(
                        this@CategorySummaryActivity,
                        android.R.layout.simple_list_item_1,
                        summary
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date. Use yyyy-MM-dd", Toast.LENGTH_SHORT).show()
        }
    }
}
