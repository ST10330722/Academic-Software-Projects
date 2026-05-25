package com.example.harvestlinkapp.ui.theme.farmer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.harvestlinkapp.data.ProduceEntity
import com.example.harvestlinkapp.databinding.ItemProduceBinding

class FarmerProduceAdapter(
    private val onItemClick: (ProduceEntity) -> Unit,
    private val onItemLongClick: (ProduceEntity) -> Unit
) : RecyclerView.Adapter<FarmerProduceAdapter.VH>() {

    private val items = mutableListOf<ProduceEntity>()

    fun submitList(list: List<ProduceEntity>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemProduceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProduceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvName.text = item.name
            tvVariety.text = "Variety: ${item.variety}"
            tvGrade.text = "Grade: ${item.grade}"
            tvQuantity.text = "${item.quantity} ${item.unit}"
            tvPrice.text = "R${item.pricePerUnit}"
            tvHarvestDate.text = "Harvest: ${item.harvestDate}"
            tvLocation.text = "Location: ${item.location}"
            tvSynced.text = if (item.isSynced) "Synced" else "Offline"

            root.setOnClickListener { onItemClick(item) }
            root.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
