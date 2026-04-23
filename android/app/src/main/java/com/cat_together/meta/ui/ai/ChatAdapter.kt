package com.cat_together.meta.ui.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cat_together.meta.databinding.ItemChatUserBinding
import com.cat_together.meta.databinding.ItemChatAiBinding
import com.cat_together.meta.model.ChatMessage
import com.cat_together.meta.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: List<ChatMessage> = emptyList()

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    inner class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.apply {
                tvContent.text = message.content
                tvTime.text = formatTime(message.timestamp)
            }
        }
    }

    inner class AiViewHolder(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.apply {
                if (message.isLoading) {
                    progressLoading.visibility = android.view.View.VISIBLE
                    tvContent.visibility = android.view.View.GONE
                    tvTime.visibility = android.view.View.GONE
                } else {
                    progressLoading.visibility = android.view.View.GONE
                    tvContent.visibility = android.view.View.VISIBLE
                    tvTime.visibility = android.view.View.VISIBLE
                    tvContent.text = message.content
                    tvTime.text = formatTime(message.timestamp)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val binding = ItemChatUserBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                UserViewHolder(binding)
            }
            else -> {
                val binding = ItemChatAiBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AiViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AiViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
}
