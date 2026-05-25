package vcmsa.projects.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class TipArticle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String?,
    val videoUrl: String?,
    val topic: String?
)
