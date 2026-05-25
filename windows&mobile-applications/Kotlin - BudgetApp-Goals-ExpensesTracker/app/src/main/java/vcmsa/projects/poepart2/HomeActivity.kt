package vcmsa.projects.poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<Button>(R.id.createCategoryButton).setOnClickListener {
            startActivity(Intent(this, CreateCategoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        findViewById<Button>(R.id.btnSetBudgetGoals).setOnClickListener {
            startActivity(Intent(this, BudgetGoalsActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewExpenses).setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewCategoryTotals).setOnClickListener {
            startActivity(Intent(this, CategoryGoalSummaryActivity::class.java))
        }
    }
}
