package com.cat_together.meta.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cat_together.meta.databinding.ItemProfileMenuBinding

class ProfileMenuAdapter(
    private val onItemClick: (ProfileMenuItem) -> Unit
) : RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder>() {

    private var items: List<ProfileMenuItem> = emptyList()

    inner class MenuViewHolder(private val binding: ItemProfileMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProfileMenuItem) {
            binding.apply {
                ivIcon.setImageResource(item.icon)
                tvTitle.text = item.title
                tvSubtitle.text = item.subtitle

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemProfileMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<ProfileMenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
