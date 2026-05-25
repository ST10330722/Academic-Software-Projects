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

class RecurringExpenseActivity : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var descInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var recurrenceTypeSpinner: Spinner
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var saveBtn: Button

    private var categories: List<Category> = emptyList()

    private val db by lazy { AppDatabase.getInstance(this) }
    private val categoryDao by lazy { db.categoryDao() }
    private val recurringDao by lazy { db.recurringExpenseDao() }
    private val expenseDao by lazy { db.expenseDao() }

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurring_expense)


        categorySpinner = findViewById(R.id.categorySpinner)
        descInput = findViewById(R.id.descriptionInput)
        amountInput = findViewById(R.id.amountInput)
        recurrenceTypeSpinner = findViewById(R.id.recurrenceTypeSpinner)
        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        saveBtn = findViewById(R.id.saveBtn)


        recurrenceTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("DAILY", "WEEKLY", "MONTHLY")
        )


        listOf(startDateInput, endDateInput).forEach { editText ->
            editText.setOnClickListener { showDatePicker(editText) }
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDatePicker(editText)
            }
        }


        lifecycleScope.launch(Dispatchers.IO) {
            categories = categoryDao.getAllCategories().first()
            withContext(Dispatchers.Main) {
                categorySpinner.adapter = ArrayAdapter(
                    this@RecurringExpenseActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    categories.map { it.name }
                )
            }
        }


        saveBtn.setOnClickListener {
            val amount = amountInput.text.toString().toDoubleOrNull()
            val startDate = startDateInput.text.toString().trim()
            val recurrenceType = recurrenceTypeSpinner.selectedItem as String

            if (amount == null || startDate.isEmpty() || categories.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val catId = categories[categorySpinner.selectedItemPosition].id
            val endDateStr = endDateInput.text.toString().ifBlank { null }

            val rec = RecurringExpense(
                categoryId = catId,
                description = descInput.text.toString(),
                amount = amount,
                recurrenceType = recurrenceType,
                startDate = startDate,
                endDate = endDateStr,
                lastLoggedDate = null
            )

            lifecycleScope.launch(Dispatchers.IO) {

                val recId = recurringDao.insert(rec)
                val today = LocalDate.now().format(fmt)
                var toastMsg = "Recurring expense saved!"

                if (rec.startDate == today) {

                    val exp = Expense(
                        id = 0L,
                        date = today,
                        startTime = today,
                        endTime = today,
                        description = "[Recurring] ${rec.description}",
                        amount = rec.amount,
                        categoryId = rec.categoryId,
                        photoPath = null
                    )
                    expenseDao.insertExpense(exp)

                    recurringDao.update(rec.copy(id = recId, lastLoggedDate = today))
                    toastMsg = "Recurring expense saved! First expense logged today."
                } else if (rec.endDate != null && rec.endDate < rec.startDate) {
                    toastMsg = "End date is before start date! Please check."
                } else {
                    toastMsg = "Recurring expense saved! First expense will log on ${rec.startDate}."
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecurringExpenseActivity, toastMsg, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showDatePicker(target: EditText) {
        val today = LocalDate.now()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = LocalDate.of(year, month + 1, dayOfMonth)
                target.setText(date.format(fmt))
            },
            today.year, today.monthValue - 1, today.dayOfMonth
        ).show()
    }
}
