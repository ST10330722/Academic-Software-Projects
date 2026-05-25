package vcmsa.projects.poepart2

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import vcmsa.projects.data.AppDatabase
import vcmsa.projects.data.TipArticle

class TipsListActivity : AppCompatActivity() {
    private lateinit var tipsListView: ListView
    private val db by lazy { AppDatabase.getInstance(this) }
    private val tipsDao by lazy { db.tipArticleDao() }
    private var tips: List<TipArticle> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_list)

        tipsListView = findViewById(R.id.tipsListView)

        lifecycleScope.launch(Dispatchers.IO) {
            tipsDao.getAllTips().collect { tipList ->
                tips = tipList
                val titles = tipList.map { it.title }
                launch(Dispatchers.Main) {
                    tipsListView.adapter = ArrayAdapter(
                        this@TipsListActivity,
                        android.R.layout.simple_list_item_1,
                        titles
                    )
                }
            }
        }

        tipsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, pos, _ ->
            val tip = tips[pos]
            val intent = Intent(this, TipDetailActivity::class.java)
            intent.putExtra("TIP_ID", tip.id)
            startActivity(intent)
        }
    }
}
