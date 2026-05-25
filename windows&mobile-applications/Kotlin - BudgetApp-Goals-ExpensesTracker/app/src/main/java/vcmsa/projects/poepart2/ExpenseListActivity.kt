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

class ExpenseListActivity : AppCompatActivity() {
    private lateinit var startDateInput : EditText
    private lateinit var endDateInput   : EditText
    private lateinit var loadExpensesBtn: Button
    private lateinit var expenseListView: ListView

    private val fmt        = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val db         by lazy { AppDatabase.getInstance(this) }
    private val expenseDao by lazy { db.expenseDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        startDateInput    = findViewById(R.id.startDateInput)
        endDateInput      = findViewById(R.id.endDateInput)
        loadExpensesBtn   = findViewById(R.id.loadExpensesBtn)
        expenseListView   = findViewById(R.id.expenseListView)

        listOf(startDateInput, endDateInput).forEach { field ->
            field.setOnClickListener { showDatePicker(field) }
        }

        loadExpensesBtn.setOnClickListener { loadFromDb() }
    }

    private fun showDatePicker(target: EditText) {
        val today = LocalDate.now()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                target.setText(LocalDate.of(y, m + 1, d).format(fmt))
            },
            today.year, today.monthValue - 1, today.dayOfMonth
        ).show()
    }

    private fun loadFromDb() {
        val start = startDateInput.text.toString().trim()
        val end   = endDateInput.text.toString().trim()
        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Enter both dates", Toast.LENGTH_SHORT).show()
            return
        }

        val sd = LocalDate.parse(start, fmt)
        val ed = LocalDate.parse(end, fmt)

        lifecycleScope.launch(Dispatchers.IO) {
            val all = expenseDao.getAllExpenses().first()
            val filtered = all.filter {
                val d = LocalDate.parse(it.date, fmt)
                !d.isBefore(sd) && !d.isAfter(ed)
            }

            withContext(Dispatchers.Main) {
                expenseListView.adapter =
                    ExpenseAdapter(this@ExpenseListActivity, filtered)
            }
        }
    }
}
