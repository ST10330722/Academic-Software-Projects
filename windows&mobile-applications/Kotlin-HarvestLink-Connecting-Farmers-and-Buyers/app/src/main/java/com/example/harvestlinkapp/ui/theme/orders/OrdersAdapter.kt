package com.example.harvestlinkapp.ui.theme.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.databinding.ItemOrderBinding
import com.example.harvestlinkapp.model.OrderItemDto

class OrdersAdapter(
    private val isFarmerView: Boolean,
    private val onChangeStatus: (orderDocId: String, newStatus: String) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.VH>() {

    private val items = mutableListOf<OrderItemDto>()

    fun submitList(list: List<OrderItemDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            if (isFarmerView) {
                binding.btnChangeStatus.visibility = View.VISIBLE
                binding.btnChangeStatus.setOnClickListener {
                    showStatusMenu()
                }
            } else {
                binding.btnChangeStatus.visibility = View.GONE
            }
        }

        private fun showStatusMenu() {
            val item = items[bindingAdapterPosition]
            val btn = binding.btnChangeStatus
            val popup = PopupMenu(btn.context, btn)
            popup.menuInflater.inflate(R.menu.menu_order_status, popup.menu)
            popup.setOnMenuItemClickListener { m ->
                val newStatus = when (m.itemId) {
                    R.id.status_accept -> "accepted"
                    R.id.status_fulfil -> "fulfilled"
                    R.id.status_cancel -> "cancelled"
                    else -> null
                }
                if (newStatus != null) {
                    onChangeStatus(item.docId, newStatus)
                }
                true
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }



    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]


        holder.binding.tvProduceName.text = item.produceName


        holder.binding.tvQuantity.text = "Qty: ${item.quantity}"


        holder.binding.tvStatus.text = "Status: ${item.status}"


        if (item.farmerName.isNotBlank()) {
            holder.binding.tvFarmerName.text = "Farmer: ${item.farmerName}"
            holder.binding.tvFarmerName.visibility = View.VISIBLE
        } else {
            holder.binding.tvFarmerName.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = items.size
}
