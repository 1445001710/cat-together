package com.cat_together.meta.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ItemCatCardBinding
import com.cat_together.meta.model.Cat

class CatAdapter(
    private val onItemClickListener: OnItemClickListener
) : ListAdapter<Cat, CatAdapter.CatViewHolder>(CatDiffCallback()) {

    interface OnItemClickListener {
        fun onItemClick(cat: Cat)
    }

    inner class CatViewHolder(private val binding: ItemCatCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cat: Cat) {
            binding.apply {
                tvCatName.text = cat.name
                tvCatBreed.text = cat.breed.ifEmpty { "未知品种" }
                tvCatAge.text = cat.ageString
                tvCatGender.text = Cat.getGenderName(cat.gender)
                tvCatWeight.text = if (cat.weight > 0) "${cat.weight}kg" else ""

                Glide.with(ivCatAvatar)
                    .load(cat.avatar?.takeIf { it.isNotEmpty() })
                    .placeholder(R.drawable.default_cat_avatar)
                    .error(R.drawable.default_cat_avatar)
                    .centerCrop()
                    .into(ivCatAvatar)

                root.setOnClickListener {
                    onItemClickListener.onItemClick(cat)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        val binding = ItemCatCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class CatDiffCallback : DiffUtil.ItemCallback<Cat>() {
        override fun areItemsTheSame(oldItem: Cat, newItem: Cat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cat, newItem: Cat): Boolean {
            return oldItem == newItem
        }
    }
}
