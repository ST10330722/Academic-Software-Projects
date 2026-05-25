package vcmsa.projects.poepart2

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import vcmsa.projects.data.AppDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecurringExpenseWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val recurringDao = db.recurringExpenseDao()
        val expenseDao = db.expenseDao()

        val today = LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val recurrences = recurringDao.getAll().first()
        for (rec in recurrences) {
            val start = LocalDate.parse(rec.startDate, fmt)
            val end = rec.endDate?.let { LocalDate.parse(it, fmt) }
            val lastLogged = rec.lastLoggedDate?.let { LocalDate.parse(it, fmt) }
            if (!today.isBefore(start) && (end == null || !today.isAfter(end))) {
                if (lastLogged == null || lastLogged.isBefore(today)) {
                    val shouldLog = when (rec.recurrenceType) {
                        "DAILY" -> lastLogged == null || lastLogged.isBefore(today)
                        "WEEKLY" -> lastLogged == null || lastLogged.plusWeeks(1) <= today
                        "MONTHLY" -> lastLogged == null || lastLogged.plusMonths(1) <= today
                        else -> false
                    }
                    if (shouldLog) {
                        val exp = Expense(
                            id = 0L,
                            categoryId = rec.categoryId,
                            date = today.format(fmt),
                            amount = rec.amount,
                            description = "[Recurring] ${rec.description}",
                            startTime = today.format(fmt),
                            endTime = today.format(fmt),
                            photoPath = null
                        )
                        expenseDao.insertExpense(exp)
                        recurringDao.update(rec.copy(lastLoggedDate = today.format(fmt)))
                    }
                }
            }
        }
        return Result.success()
    }
}
