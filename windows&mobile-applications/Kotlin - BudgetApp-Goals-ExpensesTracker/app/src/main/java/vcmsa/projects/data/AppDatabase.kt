package vcmsa.projects.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vcmsa.projects.poepart2.Expense
import vcmsa.projects.poepart2.Category
import vcmsa.projects.poepart2.RecurringExpense

@Database(
    entities = [User::class, Expense::class, Category::class, RecurringExpense::class, TipArticle::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun tipArticleDao(): TipArticleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    //This is the seed data for the tips which you will have to switch to receive from the  online DB
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).tipArticleDao().insertAll(
                                    listOf(
                                        TipArticle(
                                            title = "How to Save More Money",
                                            content = "Pay yourself first, set savings goals, automate transfers, avoid impulse buys, and track your progress.",
                                            videoUrl = null,
                                            topic = "Saving"
                                        ),
                                        TipArticle(
                                            title = "Managing Debt Effectively",
                                            content = "Make a repayment plan, prioritize high-interest debt, avoid new debt, and seek advice if needed.",
                                            videoUrl = null,
                                            topic = "Debt"
                                        ),
                                        TipArticle(
                                            title = "Budgeting Basics (Video)",
                                            content = null,
                                            videoUrl = "https://www.youtube.com/watch?v=sVKQn2I4HDM",
                                            topic = "Budgeting"
                                        )
                                    )
                                )
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
