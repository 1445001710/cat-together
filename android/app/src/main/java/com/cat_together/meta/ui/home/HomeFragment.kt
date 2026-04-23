package com.cat_together.meta.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cat_together.meta.databinding.FragmentHomeBinding
import com.cat_together.meta.model.Cat
import com.cat_together.meta.ui.cat.CatProfileActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }
    private lateinit var catAdapter: CatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.loadCats()
    }

    private fun setupRecyclerView() {
        catAdapter = CatAdapter(object : CatAdapter.OnItemClickListener {
            override fun onItemClick(cat: Cat) {
                val intent = Intent(requireContext(), CatProfileActivity::class.java)
                intent.putExtra("cat_id", cat.id)
                startActivity(intent)
            }
        })

        binding.rvCats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = catAdapter
        }
    }

    private fun setupObservers() {
        viewModel.cats.observe(viewLifecycleOwner) { cats ->
            if (cats.isEmpty()) {
                binding.rvCats.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvMyCatsTitle.visibility = View.GONE
            } else {
                binding.rvCats.visibility = View.VISIBLE
                binding.layoutEmpty.visibility = View.GONE
                binding.tvMyCatsTitle.visibility = View.VISIBLE
                catAdapter.submitList(cats)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show/hide loading indicator
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        // 添加第一只猫咪
        binding.btnAddFirstCat.setOnClickListener {
            openAddCatActivity()
        }

        // FAB添加猫咪
        binding.fabAddCat.setOnClickListener {
            openAddCatActivity()
        }

        // 快捷功能卡片
        binding.cardDaily.setOnClickListener {
            Toast.makeText(requireContext(), "今日推荐", Toast.LENGTH_SHORT).show()
        }

        binding.cardAdopt.setOnClickListener {
            Toast.makeText(requireContext(), "领养信息", Toast.LENGTH_SHORT).show()
        }

        binding.cardDiary.setOnClickListener {
            Toast.makeText(requireContext(), "猫咪日记", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddCatActivity() {
        val intent = Intent(requireContext(), CatProfileActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
