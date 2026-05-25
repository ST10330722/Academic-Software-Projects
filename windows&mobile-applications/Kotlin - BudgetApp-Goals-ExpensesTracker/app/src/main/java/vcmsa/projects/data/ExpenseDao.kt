package vcmsa.projects.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.poepart2.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, startTime DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert fun insertExpense(expense: Expense): Long
    @Update fun updateExpense(expense: Expense)
    @Delete fun deleteExpense(expense: Expense)
}
