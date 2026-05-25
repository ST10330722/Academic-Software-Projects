package vcmsa.projects.poepart2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vcmsa.projects.data.AppDatabase
import kotlinx.coroutines.flow.first
class TipDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tip_detail)

        val tipId = intent.getIntExtra("TIP_ID", -1)
        val titleView = findViewById<TextView>(R.id.tipTitle)
        val contentView = findViewById<TextView>(R.id.tipContent)
        val videoBtn = findViewById<Button>(R.id.openVideoBtn)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@TipDetailActivity)
            val tip = db.tipArticleDao().getAllTips().first().find { it.id == tipId }
            launch(Dispatchers.Main) {
                if (tip != null) {
                    titleView.text = tip.title
                    if (tip.content != null) {
                        contentView.text = tip.content
                        videoBtn.isEnabled = false
                        videoBtn.text = "No video"
                    } else if (tip.videoUrl != null) {
                        contentView.text = "(This is a video tip.)"
                        videoBtn.isEnabled = true
                        videoBtn.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tip.videoUrl))
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}
