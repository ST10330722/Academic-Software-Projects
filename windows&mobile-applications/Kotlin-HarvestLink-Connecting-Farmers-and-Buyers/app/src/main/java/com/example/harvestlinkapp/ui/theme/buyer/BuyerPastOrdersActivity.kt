package com.example.harvestlinkapp.ui.theme.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.databinding.ActivityBuyerOrdersBinding
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.example.harvestlinkapp.ui.theme.orders.OrdersAdapter
import kotlinx.coroutines.launch

class BuyerPastOrdersActivity : LocalizedActivity() {

    private lateinit var binding: ActivityBuyerOrdersBinding
    private lateinit var adapter: OrdersAdapter
    private val apiRepo by lazy { ApiRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyerOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.past_orders_title)


        adapter = OrdersAdapter(
            isFarmerView = false,
            onChangeStatus = { _, _ -> }
        )

        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = adapter

        loadPastOrders()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadPastOrders() {
        lifecycleScope.launch {
            try {
                val list = apiRepo.getPastOrdersForRole("buyer")
                adapter.submitList(list)
                binding.tvEmpty.visibility =
                    if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    this@BuyerPastOrdersActivity,
                    getString(R.string.error_generic, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
