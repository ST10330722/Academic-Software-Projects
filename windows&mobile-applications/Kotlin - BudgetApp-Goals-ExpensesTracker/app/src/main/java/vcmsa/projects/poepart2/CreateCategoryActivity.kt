package vcmsa.projects.poepart2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import vcmsa.projects.poepart2.R

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var categoryNameInput: EditText
    private lateinit var categoryDescriptionInput: EditText
    private lateinit var saveCategoryButton: Button

    // Firestore reference
    private val firestore = Firebase.firestore
    private val uid = Firebase.auth.currentUser?.uid.orEmpty()
    private val categoriesCol = firestore
        .collection("users")
        .document(uid)
        .collection("categories")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        categoryNameInput = findViewById(R.id.categoryNameInput)
        categoryDescriptionInput = findViewById(R.id.categoryDescriptionInput)
        saveCategoryButton = findViewById(R.id.saveCategoryButton)

        saveCategoryButton.setOnClickListener {
            val name = categoryNameInput.text.toString().trim()
            val description = categoryDescriptionInput.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to Firestore
            val data = mapOf(
                "name" to name,
                "description" to description
            )
            categoriesCol.add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "Category saved online", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Failed to create category online: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
