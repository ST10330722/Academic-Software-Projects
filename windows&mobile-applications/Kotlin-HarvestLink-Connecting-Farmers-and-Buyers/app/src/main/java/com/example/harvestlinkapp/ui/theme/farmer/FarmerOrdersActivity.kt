package com.example.harvestlinkapp.ui.theme.farmer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.ApiRepository
import com.example.harvestlinkapp.databinding.ActivityFarmerOrdersBinding
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.example.harvestlinkapp.ui.theme.orders.OrdersAdapter
import kotlinx.coroutines.launch

class FarmerOrdersActivity : LocalizedActivity(){

    private lateinit var binding: ActivityFarmerOrdersBinding
    private lateinit var adapter: OrdersAdapter
    private val apiRepo by lazy { ApiRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFarmerOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.farmer_orders_title)

        adapter = OrdersAdapter(
            isFarmerView = true,
            onChangeStatus = { orderDocId, newStatus ->
                updateStatus(orderDocId, newStatus)
            }
        )

        binding.recyclerOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerOrders.adapter = adapter

        loadOrders()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                val list = apiRepo.getOrdersForRole("farmer")
                adapter.submitList(list)
                binding.tvEmpty.visibility =
                    if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            } catch (e: Exception) {
                Toast.makeText(
                    this@FarmerOrdersActivity,
                    getString(R.string.error_generic, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateStatus(orderDocId: String, newStatus: String) {
        lifecycleScope.launch {
            try {
                apiRepo.updateOrderStatus(orderDocId, newStatus)
                Toast.makeText(
                    this@FarmerOrdersActivity,
                    getString(R.string.status_updated),
                    Toast.LENGTH_SHORT
                ).show()

                loadOrders()
            } catch (e: Exception) {
                Toast.makeText(
                    this@FarmerOrdersActivity,
                    getString(R.string.status_update_failed, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
}
