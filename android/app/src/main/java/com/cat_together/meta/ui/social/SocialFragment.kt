package com.cat_together.meta.ui.social

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cat_together.meta.databinding.FragmentSocialBinding

class SocialFragment : Fragment() {

    private var _binding: FragmentSocialBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SocialViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SocialViewModel::class.java]
    }

    private fun setupViews() {
        postAdapter = PostAdapter { post, action ->
            when (action) {
                "like" -> {
                    // TODO: Handle like
                }
                "comment" -> {
                    // TODO: Handle comment
                }
            }
        }

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }

        binding.fabPost.setOnClickListener {
            // TODO: Open post dialog
            Toast.makeText(requireContext(), "发布动态功能开发中", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
