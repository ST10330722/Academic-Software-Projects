package vcmsa.projects.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.poepart2.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert fun insertCategory(category: Category): Long
    @Update fun updateCategory(category: Category)
    @Delete fun deleteCategory(category: Category)
}
