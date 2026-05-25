package com.example.harvestlinkapp.ui.theme.buyer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.harvestlinkapp.databinding.ItemProduceBuyerBinding
import com.example.harvestlinkapp.model.ProduceDto

class BuyerProduceAdapter(
    private val onClick: (ProduceDto) -> Unit
) : RecyclerView.Adapter<BuyerProduceAdapter.VH>() {

    private val items = mutableListOf<ProduceDto>()

    fun submitList(list: List<ProduceDto>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemProduceBuyerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val item = items[bindingAdapterPosition]
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProduceBuyerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvGrade.text = item.grade
        holder.binding.tvQuantity.text = "${item.quantity} ${item.unit}"
        holder.binding.tvPrice.text = "R${item.pricePerUnit}"
    }

    override fun getItemCount(): Int = items.size
}
