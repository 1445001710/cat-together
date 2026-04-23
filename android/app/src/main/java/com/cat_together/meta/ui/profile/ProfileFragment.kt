package com.cat_together.meta.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.R
import com.cat_together.meta.databinding.FragmentProfileBinding
import com.cat_together.meta.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel
    private lateinit var menuAdapter: ProfileMenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupViewModel()
            setupRecyclerView()
            setupObservers()
            setupClickListeners()

            // 同步加载用户信息
            loadUserInfo()
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ProfileFragment", "Error in onViewCreated", e)
        }
    }

    private fun loadUserInfo() {
        val user = CatTogetherApp.sharedPreferencesHelper.user
        if (user != null) {
            binding.tvNickname.text = user.nickname
        } else {
            binding.tvNickname.text = "未登录"
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
    }

    private fun setupRecyclerView() {
        val menuItems = listOf(
            ProfileMenuItem(
                icon = R.drawable.ic_album,
                title = "相册",
                subtitle = "查看猫咪照片和视频"
            ),
            ProfileMenuItem(
                icon = R.drawable.ic_recharge,
                title = "充值中心",
                subtitle = "充值会员和积分"
            ),
            ProfileMenuItem(
                icon = R.drawable.ic_manual,
                title = "使用手册",
                subtitle = "查看使用说明"
            ),
            ProfileMenuItem(
                icon = R.drawable.ic_settings,
                title = "设置",
                subtitle = "应用设置"
            )
        )

        menuAdapter = ProfileMenuAdapter { item ->
            when (item.title) {
                "相册" -> openAlbumActivity()
                "充值中心" -> openRechargeActivity()
                "使用手册" -> openManualActivity()
                "设置" -> openSettingsActivity()
            }
        }

        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = menuAdapter
        }

        menuAdapter.submitList(menuItems)
    }

    private fun setupObservers() {
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvNickname.text = it.nickname
            }
        }

        viewModel.logoutResult.observe(viewLifecycleOwner) { result ->
            if (result) {
                Toast.makeText(requireContext(), "退出成功", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), "退出失败", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }
    }

    private fun showLogoutConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("退出登录")
            .setMessage(R.string.logout_confirm)
            .setPositiveButton("确定") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openAlbumActivity() {
        // TODO: 打开相册页面
        Toast.makeText(requireContext(), "相册功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun openRechargeActivity() {
        // TODO: 打开充值中心页面
        Toast.makeText(requireContext(), "充值中心功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun openManualActivity() {
        // TODO: 打开使用手册页面
        Toast.makeText(requireContext(), "使用手册功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun openSettingsActivity() {
        // TODO: 打开设置页面
        Toast.makeText(requireContext(), "设置功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ProfileMenuItem(
    val icon: Int,
    val title: String,
    val subtitle: String
)
