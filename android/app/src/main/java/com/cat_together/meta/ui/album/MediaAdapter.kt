package com.cat_together.meta.ui.album

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cat_together.meta.databinding.ItemMediaBinding
import com.cat_together.meta.model.Media

class MediaAdapter(
    private val onItemClick: (Media) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var mediaList: List<Media> = emptyList()

    inner class MediaViewHolder(private val binding: ItemMediaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(media: Media) {
            binding.apply {
                if (media.url.isNotEmpty()) {
                    Glide.with(ivMedia)
                        .load(media.url)
                        .centerCrop()
                        .into(ivMedia)
                }

                if (media.type == Media.TYPE_VIDEO) {
                    ivVideoPlay.visibility = android.view.View.VISIBLE
                } else {
                    ivVideoPlay.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(media)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    override fun getItemCount(): Int = mediaList.size

    fun submitList(newMediaList: List<Media>) {
        mediaList = newMediaList
        notifyDataSetChanged()
    }
}
