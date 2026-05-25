package vcmsa.projects.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    fun authenticate(email: String, password: String): User?
}
