package vcmsa.projects.poepart2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Int,
    val description: String,
    val amount: Double,
    val recurrenceType: String,
    val startDate: String,
    val endDate: String?,
    val lastLoggedDate: String?
)
