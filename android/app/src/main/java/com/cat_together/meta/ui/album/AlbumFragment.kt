package com.cat_together.meta.ui.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.cat_together.meta.databinding.FragmentAlbumBinding

class AlbumFragment : Fragment() {

    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AlbumViewModel
    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupViews()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[AlbumViewModel::class.java]
    }

    private fun setupViews() {
        mediaAdapter = MediaAdapter { media ->
            // TODO: Handle media click
        }

        binding.rvMedia.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = mediaAdapter
        }

        binding.fabUpload.setOnClickListener {
            // TODO: Open upload dialog
            Toast.makeText(requireContext(), "上传功能开发中", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
