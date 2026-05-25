
package vcmsa.projects.poepart2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val amount: Double,
    val categoryId: Int,
    val photoPath: String? = null
)
