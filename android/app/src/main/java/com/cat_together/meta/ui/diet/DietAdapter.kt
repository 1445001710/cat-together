package com.cat_together.meta.ui.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cat_together.meta.databinding.ItemDietRecordBinding
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.utils.DateUtils

class DietAdapter(
    private val onItemClick: (DietRecord) -> Unit
) : RecyclerView.Adapter<DietAdapter.DietViewHolder>() {

    private var records: List<DietRecord> = emptyList()

    inner class DietViewHolder(private val binding: ItemDietRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: DietRecord) {
            binding.apply {
                tvTypeIcon.text = DietRecord.getTypeIcon(record.type)
                tvTypeName.text = DietRecord.getTypeName(record.type)
                tvAmount.text = if (record.type == DietRecord.TYPE_FOOD) {
                    "${record.amount}克"
                } else {
                    "${record.amount}次"
                }
                tvTime.text = DateUtils.formatDateTime(record.timestamp)

                root.setOnClickListener {
                    onItemClick(record)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DietViewHolder {
        val binding = ItemDietRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DietViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DietViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    fun submitList(newRecords: List<DietRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
