package vcmsa.projects.poepart2

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1000
    private var selectedImageUri: Uri? = null

    private lateinit var categorySpinner: Spinner
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var selectImageBtn: Button
    private lateinit var saveExpenseBtn: Button

    private val categoryMap = mutableMapOf<String, String>()  // name → docId
    private val db = Firebase.firestore
    private val uid get() = Firebase.auth.currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Bind views
        categorySpinner  = findViewById(R.id.categorySpinner)
        startDateInput   = findViewById(R.id.startDateInput)
        endDateInput     = findViewById(R.id.endDateInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        imagePreview     = findViewById(R.id.imagePreview)
        selectImageBtn   = findViewById(R.id.selectImageBtn)
        saveExpenseBtn   = findViewById(R.id.saveExpenseBtn)

        // Load categories into spinner
        loadCategories()

        // Show DatePicker on date fields
        startDateInput.setOnClickListener {
            showDatePickerDialog(startDateInput)
        }
        endDateInput.setOnClickListener {
            showDatePickerDialog(endDateInput)
        }

        // Image picker
        selectImageBtn.setOnClickListener {
            val pick = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pick, IMAGE_PICK_CODE)
        }

        // Save expense
        saveExpenseBtn.setOnClickListener {
            saveExpense()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(
            this,
            { _, year, month, day ->
                val mm = month + 1
                val dateStr = String.format("%04d-%02d-%02d", year, mm, day)
                editText.setText(dateStr)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    private fun loadCategories() {
        db.collection("users").document(uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { snap ->
                val names = snap.documents.mapNotNull { it.getString("name") }
                snap.documents.forEach { doc ->
                    doc.getString("name")?.let { categoryMap[it] = doc.id }
                }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    names
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data?.data != null) {
            selectedImageUri = data.data!!
            imagePreview.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    private fun saveExpense() {
        val startDate   = startDateInput.text.toString().trim()
        val endDate     = endDateInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedName = categorySpinner.selectedItem as? String
        val categoryId   = selectedName?.let { categoryMap[it] }
        if (categoryId == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "startDate"   to startDate,
            "endDate"     to endDate,
            "description" to description,
            "categoryId"  to categoryId,
            "photoUri"    to selectedImageUri?.toString()
        )

        db.collection("users").document(uid)
            .collection("expenses")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
