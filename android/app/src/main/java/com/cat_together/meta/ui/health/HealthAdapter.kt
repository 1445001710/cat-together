package com.cat_together.meta.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cat_together.meta.databinding.ItemHealthRecordBinding
import com.cat_together.meta.model.HealthRecord
import com.cat_together.meta.utils.DateUtils

class HealthAdapter(
    private val onItemClick: (HealthRecord) -> Unit
) : RecyclerView.Adapter<HealthAdapter.HealthViewHolder>() {

    private var records: List<HealthRecord> = emptyList()

    inner class HealthViewHolder(private val binding: ItemHealthRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: HealthRecord) {
            binding.apply {
                tvRecordType.text = HealthRecord.getTypeName(record.recordType)
                tvValue.text = "${record.value}${HealthRecord.getUnit(record.recordType)}"
                tvDate.text = DateUtils.formatDate(record.recordDate)

                if (record.note.isNotEmpty()) {
                    tvNote.text = record.note
                    tvNote.visibility = android.view.View.VISIBLE
                } else {
                    tvNote.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(record)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthViewHolder {
        val binding = ItemHealthRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    fun submitList(newRecords: List<HealthRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }
}
