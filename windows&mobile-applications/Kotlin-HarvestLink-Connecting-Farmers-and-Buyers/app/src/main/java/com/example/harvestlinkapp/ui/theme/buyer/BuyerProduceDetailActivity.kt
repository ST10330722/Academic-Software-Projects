package com.example.harvestlinkapp.ui.theme.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.databinding.ActivityBuyerProduceDetailBinding
import com.example.harvestlinkapp.model.OrderDto
import com.example.harvestlinkapp.ui.LocalizedActivity
import kotlinx.coroutines.launch

class BuyerProduceDetailActivity : LocalizedActivity() {

    private lateinit var binding: ActivityBuyerProduceDetailBinding
    private val apiRepo by lazy { ApiRepository() }

    private var produceDocId: String? = null
    private var farmerUid: String? = null
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyerProduceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.produce_details_title)

        produceDocId = intent.getStringExtra(EXTRA_DOC_ID)
        farmerUid = intent.getStringExtra(EXTRA_FARMER_UID)
        val name = intent.getStringExtra(EXTRA_NAME) ?: ""
        val grade = intent.getStringExtra(EXTRA_GRADE) ?: ""
        val quantity = intent.getDoubleExtra(EXTRA_QUANTITY, 0.0)
        val unit = intent.getStringExtra(EXTRA_UNIT) ?: ""
        val price = intent.getDoubleExtra(EXTRA_PRICE, 0.0)
        val harvestDate = intent.getStringExtra(EXTRA_HARVEST_DATE) ?: ""
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        binding.tvName.text = name
        binding.tvGrade.text = grade
        binding.tvQuantity.text = getString(R.string.quantity_with_unit, quantity, unit)
        binding.tvPrice.text = getString(R.string.price_per_unit, price, unit)
        binding.tvHarvestDate.text = getString(R.string.harvest_date_label, harvestDate)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imgPreview)
        }

        binding.btnOrder.setOnClickListener {
            placeOrder()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun placeOrder() {
        val docId = produceDocId
        val farmer = farmerUid
        if (docId == null || farmer == null) {
            Toast.makeText(
                this,
                getString(R.string.error_missing_product_info),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val qtyText = binding.etQuantity.text.toString().trim()
        val qty = qtyText.toDoubleOrNull()
        val note = binding.etNote.text.toString().trim().ifEmpty { null }

        if (qty == null || qty <= 0) {
            Toast.makeText(
                this,
                getString(R.string.error_enter_valid_quantity),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val order = OrderDto(
                    produceDocId = docId,
                    farmerUid = farmer,
                    quantity = qty,
                    note = note
                )
                apiRepo.createOrder(order)
                Toast.makeText(
                    this@BuyerProduceDetailActivity,
                    getString(R.string.order_placed),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@BuyerProduceDetailActivity,
                    getString(R.string.error_generic, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_DOC_ID = "extra_doc_id"
        const val EXTRA_FARMER_UID = "extra_farmer_uid"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_GRADE = "extra_grade"
        const val EXTRA_QUANTITY = "extra_quantity"
        const val EXTRA_UNIT = "extra_unit"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_HARVEST_DATE = "extra_harvest_date"
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }
}
