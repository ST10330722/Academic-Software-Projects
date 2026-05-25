package vcmsa.projects.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.poepart2.RecurringExpense
@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses")
    fun getAll(): Flow<List<RecurringExpense>>

    @Insert
    fun insert(recurringExpense: RecurringExpense): Long

    @Update
    fun update(recurringExpense: RecurringExpense)

    @Delete
    fun delete(recurringExpense: RecurringExpense)
}
