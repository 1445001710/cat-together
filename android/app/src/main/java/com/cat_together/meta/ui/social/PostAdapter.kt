package com.cat_together.meta.ui.social

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cat_together.meta.databinding.ItemPostBinding
import com.cat_together.meta.model.Post

class PostAdapter(
    private val onActionClick: (Post, String) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private var posts: List<Post> = emptyList()

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                tvUserName.text = post.user?.nickname ?: "未知用户"
                tvContent.text = post.content
                tvTime.text = post.createTimeAgo
                tvLikeCount.text = "${post.likeCount}"
                tvCommentCount.text = "${post.commentCount}"

                // Load user avatar
                if (post.user?.avatar?.isNotEmpty() == true) {
                    Glide.with(ivUserAvatar)
                        .load(post.user.avatar)
                        .circleCrop()
                        .placeholder(com.cat_together.meta.R.mipmap.ic_launcher)
                        .into(ivUserAvatar)
                }

                // Like button state
                if (post.isLiked) {
                    ivLike.setImageResource(android.R.drawable.star_big_on)
                } else {
                    ivLike.setImageResource(android.R.drawable.star_big_off)
                }

                ivLike.setOnClickListener {
                    onActionClick(post, "like")
                }

                ivComment.setOnClickListener {
                    onActionClick(post, "comment")
                }

                root.setOnClickListener {
                    // TODO: Navigate to post detail
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun submitList(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
