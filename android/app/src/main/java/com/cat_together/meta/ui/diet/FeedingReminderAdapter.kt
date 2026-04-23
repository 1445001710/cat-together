package com.cat_together.meta.ui.diet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cat_together.meta.databinding.ItemFeedingReminderBinding
import com.cat_together.meta.model.FeedingReminder

class FeedingReminderAdapter(
    private val onEditClick: (FeedingReminder) -> Unit,
    private val onDeleteClick: (FeedingReminder) -> Unit,
    private val onEnabledChanged: (FeedingReminder, Boolean) -> Unit
) : RecyclerView.Adapter<FeedingReminderAdapter.ReminderViewHolder>() {

    private var reminders: List<FeedingReminder> = emptyList()

    inner class ReminderViewHolder(private val binding: ItemFeedingReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: FeedingReminder) {
            binding.apply {
                tvTime.text = reminder.time
                tvType.text = getTypeName(reminder.type)
                tvRepeat.text = getRepeatRuleName(reminder.repeatRule)

                // 先移除监听器，避免 isChecked 赋值时触发监听器
                switchEnabled.setOnCheckedChangeListener(null)
                switchEnabled.isChecked = reminder.enabled

                switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                    onEnabledChanged(reminder, isChecked)
                }

                btnEdit.setOnClickListener {
                    onEditClick(reminder)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(reminder)
                }
            }
        }

        private fun getTypeName(type: Int): String {
            return when (type) {
                1 -> "喝水"
                2 -> "猫粮"
                3 -> "零食"
                4 -> "猫条"
                else -> "其他"
            }
        }

        private fun getRepeatRuleName(rule: String): String {
            return when (rule) {
                FeedingReminder.REPEAT_DAILY -> "每天重复"
                FeedingReminder.REPEAT_WEEKLY -> "每周重复"
                FeedingReminder.REPEAT_MONTHLY -> "每月重复"
                else -> "不重复"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemFeedingReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])
    }

    override fun getItemCount(): Int = reminders.size

    fun submitList(newReminders: List<FeedingReminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}
