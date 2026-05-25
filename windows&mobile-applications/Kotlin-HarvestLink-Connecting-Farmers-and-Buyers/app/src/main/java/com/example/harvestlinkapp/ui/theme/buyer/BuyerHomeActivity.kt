package com.example.harvestlinkapp.ui.theme.buyer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.databinding.ActivityBuyerHomeBinding
import com.example.harvestlinkapp.model.ProduceDto
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.example.harvestlinkapp.ui.theme.settings.SettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class BuyerHomeActivity : LocalizedActivity() {

    private lateinit var binding: ActivityBuyerHomeBinding
    private lateinit var adapter: BuyerProduceAdapter

    private val apiRepo by lazy { ApiRepository() }
    private var currentListings: List<ProduceDto> = emptyList()

    private val screenScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.buyer_home_title)

        setupGradeSpinner()
        setupRecycler()
        setupClicks()

        loadProduceFromServer()
    }

    private fun setupRecycler() {
        adapter = BuyerProduceAdapter { listing ->
            openDetails(listing)
        }
        binding.recyclerProduce.layoutManager = LinearLayoutManager(this)
        binding.recyclerProduce.adapter = adapter
    }

    private fun setupGradeSpinner() {
        val gradeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.grades_array,
            android.R.layout.simple_spinner_item
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spGrade.adapter = gradeAdapter
    }

    private fun setupClicks() {
        binding.btnMyOrders.setOnClickListener {
            startActivity(Intent(this, BuyerOrdersActivity::class.java))
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }

        binding.btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun loadProduceFromServer() {
        screenScope.launch {
            try {
                val list = apiRepo.getAllProduce()
                currentListings = list


                applyFilters()

                binding.tvEmpty.visibility =
                    if (currentListings.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    this@BuyerHomeActivity,
                    getString(R.string.error_loading_produce, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyFilters() {
        var filtered = currentListings


        val nameFilter = binding.etFilterName.text.toString().trim()
        if (nameFilter.isNotEmpty()) {
            filtered = filtered.filter { produce ->
                produce.name.contains(nameFilter, ignoreCase = true)
            }
        }


        val minPriceText = binding.etMinPrice.text.toString().trim()
        val maxPriceText = binding.etMaxPrice.text.toString().trim()

        val minPrice = minPriceText.toDoubleOrNull()
        val maxPrice = maxPriceText.toDoubleOrNull()

        if (minPrice != null) {
            filtered = filtered.filter { it.pricePerUnit >= minPrice }
        }
        if (maxPrice != null) {
            filtered = filtered.filter { it.pricePerUnit <= maxPrice }
        }


        val selectedGrade = binding.spGrade.selectedItem?.toString() ?: ""
        val allGradesLabel = getString(R.string.filter_grade_all)

        if (selectedGrade.isNotEmpty() && selectedGrade != allGradesLabel) {
            filtered = filtered.filter { it.grade.equals(selectedGrade, ignoreCase = true) }
        }

        adapter.submitList(filtered)
        binding.tvEmpty.visibility =
            if (filtered.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun clearFilters() {
        binding.etFilterName.setText("")
        binding.etMinPrice.setText("")
        binding.etMaxPrice.setText("")
        binding.spGrade.setSelection(0)

        adapter.submitList(currentListings)
        binding.tvEmpty.visibility =
            if (currentListings.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun openDetails(produce: ProduceDto) {
        val intent = Intent(this, BuyerProduceDetailActivity::class.java).apply {
            putExtra(BuyerProduceDetailActivity.EXTRA_DOC_ID, produce.docId)
            putExtra(BuyerProduceDetailActivity.EXTRA_FARMER_UID, produce.farmerUid)
            putExtra(BuyerProduceDetailActivity.EXTRA_NAME, produce.name)
            putExtra(BuyerProduceDetailActivity.EXTRA_GRADE, produce.grade)
            putExtra(BuyerProduceDetailActivity.EXTRA_QUANTITY, produce.quantity)
            putExtra(BuyerProduceDetailActivity.EXTRA_UNIT, produce.unit)
            putExtra(BuyerProduceDetailActivity.EXTRA_PRICE, produce.pricePerUnit)
            putExtra(BuyerProduceDetailActivity.EXTRA_HARVEST_DATE, produce.harvestDate)
            putExtra(BuyerProduceDetailActivity.EXTRA_IMAGE_URL, produce.imageUrl)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_buyer_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.menu_past_orders -> {
                startActivity(Intent(this, BuyerPastOrdersActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
