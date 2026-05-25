package vcmsa.projects.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TipArticleDao {
    @Query("SELECT * FROM tips ORDER BY id DESC")
    fun getAllTips(): Flow<List<TipArticle>>

    @Insert
    fun insertTip(tip: TipArticle): Long

    @Insert
    fun insertAll(tips: List<TipArticle>)

    @Delete
    fun deleteTip(tip: TipArticle)
}
