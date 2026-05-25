package com.example.harvestlinkapp.ui.theme.farmer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.data.AppDatabase
import com.example.harvestlinkapp.data.ProduceEntity
import com.example.harvestlinkapp.databinding.ActivityFarmerHomeBinding
import com.example.harvestlinkapp.sync.SyncWorker
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.example.harvestlinkapp.ui.theme.settings.SettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FarmerHomeActivity : LocalizedActivity() {

    private lateinit var binding: ActivityFarmerHomeBinding
    private lateinit var adapter: FarmerProduceAdapter

    private val localDb by lazy { AppDatabase.getInstance(this) }
    private val apiRepo by lazy { ApiRepository() }

    private val screenScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFarmerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.farmer_home_title)

        adapter = FarmerProduceAdapter(
            onItemClick = { entity ->
                val i = Intent(this, AddEditProduceActivity::class.java).apply {
                    putExtra(AddEditProduceActivity.EXTRA_LOCAL_ID, entity.localId)
                    putExtra(AddEditProduceActivity.EXTRA_SERVER_ID, entity.serverId)
                }
                startActivity(i)
            },
            onItemLongClick = { entity ->
                confirmDelete(entity)
            }
        )

        binding.recyclerProduce.layoutManager = LinearLayoutManager(this)
        binding.recyclerProduce.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditProduceActivity::class.java))
        }


        binding.btnSync.setOnClickListener {
            val req = OneTimeWorkRequestBuilder<SyncWorker>().build()
            val wm = WorkManager.getInstance(this)

            wm.enqueue(req)
            Toast.makeText(
                this,
                getString(R.string.sync_requested),
                Toast.LENGTH_SHORT
            ).show()


            wm.getWorkInfoByIdLiveData(req.id).observe(this) { info ->
                if (info != null && info.state.isFinished) {

                    refreshFromServer()
                }
            }
        }

        loadLocalProduce()
        refreshFromServer()
    }

    override fun onResume() {
        super.onResume()

        loadLocalProduce()
    }

    private fun loadLocalProduce() {
        screenScope.launch(Dispatchers.IO) {
            val list = localDb.produceDao().getAll()
            withContext(Dispatchers.Main) {
                adapter.submitList(list)
                binding.tvEmpty.visibility =
                    if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun refreshFromServer() {
        screenScope.launch {
            try {
                val remoteList = apiRepo.getAllProduce()

                withContext(Dispatchers.IO) {
                    localDb.produceDao().clearSynced()
                    remoteList.forEach { dto ->
                        localDb.produceDao().insert(
                            ProduceEntity(
                                serverId = dto.docId,
                                name = dto.name,
                                variety = dto.variety,
                                grade = dto.grade,
                                quantity = dto.quantity,
                                unit = dto.unit,
                                pricePerUnit = dto.pricePerUnit,
                                harvestDate = dto.harvestDate,
                                location = dto.location,
                                imageUrl = dto.imageUrl,
                                isSynced = true
                            )
                        )
                    }
                }
                loadLocalProduce()
            } catch (e: Exception) {
                Toast.makeText(
                    this@FarmerHomeActivity,
                    getString(R.string.error_fetching_produce, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmDelete(entity: ProduceEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_produce_title))
            .setMessage(getString(R.string.delete_produce_message, entity.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteProduce(entity)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteProduce(entity: ProduceEntity) {
        screenScope.launch {
            try {
                entity.serverId?.let { apiRepo.deleteProduce(it) }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FarmerHomeActivity,
                    getString(R.string.error_generic, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }

            withContext(Dispatchers.IO) {
                localDb.produceDao().deleteById(entity.localId)
            }
            loadLocalProduce()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_farmer_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_orders -> {
                startActivity(Intent(this, FarmerOrdersActivity::class.java))
                true
            }
            R.id.menu_past_orders -> {
                startActivity(Intent(this, FarmerPastOrdersActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
