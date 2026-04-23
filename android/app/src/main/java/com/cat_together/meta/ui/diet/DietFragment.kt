package com.cat_together.meta.ui.diet

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.R
import com.cat_together.meta.databinding.FragmentDietBinding
import com.cat_together.meta.model.Cat
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.model.FeedingReminder
import com.cat_together.meta.network.ChatRequest
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.ui.report.ReportPreviewActivity
import com.cat_together.meta.utils.ReminderScheduler
import com.cat_together.meta.utils.ReportGenerator
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DietFragment : Fragment() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "通知权限已授予，提醒功能已启用", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "通知权限被拒绝，将无法收到提醒", Toast.LENGTH_LONG).show()
        }
    }

    private val reminderActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // AddReminderActivity 返回后不需要额外刷新，ViewModel 已经直接更新了 LiveData
    }

    private var _binding: FragmentDietBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DietViewModel
    private lateinit var dietAdapter: DietAdapter
    private lateinit var reminderAdapter: FeedingReminderAdapter
    private var currentCatId: String? = null
    private var currentTab = 0 // 0: 饮食记录, 1: 喂食提醒
    private var cats: List<Cat> = emptyList()
    private val catViews = mutableMapOf<String, View>()
    private var isInitialLoad = true  // 标记是否首次加载

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDietBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupTabLayout()
        setupRecyclerViews()
        setupObservers()
        setupClickListeners()

        // 加载猫咪列表
        loadCats()
    }

    private fun loadCats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                cats = CatTogetherApp.database.catDao().getAllCatsList()
                withContext(Dispatchers.Main) {
                    if (cats.isNotEmpty()) {
                        currentCatId = cats[0].id
                        setupCatSelector()
                        viewModel.setCurrentCat(currentCatId!!, isInitialLoad)
                        isInitialLoad = false
                    } else {
                        Toast.makeText(requireContext(), "请先添加猫咪", Toast.LENGTH_SHORT).show()
                    }
                    // 检查通知权限
                    checkNotificationPermission()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "加载猫咪失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCatSelector() {
        binding.llCatContainer.removeAllViews()
        catViews.clear()

        cats.forEach { cat ->
            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_cat_avatar_selector, binding.llCatContainer, false)

            val ivAvatar = itemView.findViewById<ImageView>(R.id.ivCatAvatar)
            val tvName = itemView.findViewById<TextView>(R.id.tvCatName)
            val selectedIndicator = itemView.findViewById<View>(R.id.selectedIndicator)

            tvName.text = cat.name

            Glide.with(ivAvatar)
                .load(cat.avatar?.takeIf { !it.isNullOrEmpty() })
                .placeholder(R.drawable.default_cat_avatar)
                .error(R.drawable.default_cat_avatar)
                .circleCrop()
                .into(ivAvatar)

            if (cat.id == currentCatId) {
                selectedIndicator.visibility = View.VISIBLE
                tvName.setTextColor(resources.getColor(R.color.primary_pink, null))
            } else {
                selectedIndicator.visibility = View.GONE
                tvName.setTextColor(resources.getColor(R.color.text_secondary, null))
            }

            itemView.setOnClickListener {
                selectCat(cat.id)
            }

            catViews[cat.id] = itemView
            binding.llCatContainer.addView(itemView)
        }
    }

    private fun selectCat(catId: String) {
        if (currentCatId == catId) return

        currentCatId = catId

        catViews.forEach { (id, view) ->
            val selectedIndicator = view.findViewById<View>(R.id.selectedIndicator)
            val tvName = view.findViewById<TextView>(R.id.tvCatName)
            if (id == catId) {
                selectedIndicator.visibility = View.VISIBLE
                tvName.setTextColor(resources.getColor(R.color.primary_pink, null))
            } else {
                selectedIndicator.visibility = View.GONE
                tvName.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        viewModel.setCurrentCat(catId)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 权限已授予
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(requireContext())
                        .setTitle("需要通知权限")
                        .setMessage("喂食提醒需要通知权限来提醒您给猫咪喂食，是否授权？")
                        .setPositiveButton("授权") { _, _ ->
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("取消", null)
                        .show()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // 检查精确闹钟权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!ReminderScheduler.canScheduleExactAlarms(requireContext())) {
                AlertDialog.Builder(requireContext())
                    .setTitle("需要闹钟权限")
                    .setMessage("喂食提醒需要精确闹钟权限来准时提醒您，是否去授权？")
                    .setPositiveButton("去授权") { _, _ ->
                        ReminderScheduler.openExactAlarmSettings(requireContext())
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[DietViewModel::class.java]
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTab = it.position
                    updateTabVisibility()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        dietAdapter = DietAdapter { record ->
            // 点击查看/编辑记录
            showRecordOptionsDialog(record)
        }

        binding.rvDietRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = dietAdapter
        }

        reminderAdapter = FeedingReminderAdapter(
            onEditClick = { reminder -> openEditReminderActivity(reminder) },
            onDeleteClick = { reminder -> confirmDeleteReminder(reminder) },
            onEnabledChanged = { reminder, enabled -> viewModel.updateReminderEnabled(reminder, enabled) }
        )

        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reminderAdapter
        }
    }

    private fun setupObservers() {
        viewModel.records.observe(viewLifecycleOwner) { records ->
            if (records == null) return@observe
            if (currentTab == 0) {
                if (records.isEmpty()) {
                    binding.llEmptyDiet.visibility = View.VISIBLE
                    binding.rvDietRecords.visibility = View.GONE
                } else {
                    binding.llEmptyDiet.visibility = View.GONE
                    binding.rvDietRecords.visibility = View.VISIBLE
                }
            }
            dietAdapter.submitList(records)
        }

        viewModel.reminders.observe(viewLifecycleOwner) { reminders ->
            if (reminders == null) return@observe
            if (currentTab == 1) {
                if (reminders.isEmpty()) {
                    binding.llEmptyReminder.visibility = View.VISIBLE
                    binding.rvReminders.visibility = View.GONE
                } else {
                    binding.llEmptyReminder.visibility = View.GONE
                    binding.rvReminders.visibility = View.VISIBLE
                }
            }
            reminderAdapter.submitList(reminders)
            // 调度提醒闹钟
            scheduleReminders(reminders)
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
        binding.fabAddRecord.setOnClickListener {
            if (currentTab == 0) {
                openAddRecordActivity()
            } else {
                openAddReminderActivity()
            }
        }

        binding.btnAddFirstReminder.setOnClickListener {
            openAddReminderActivity()
        }

        // 饮食分析VIP按钮
        binding.btnDietAnalysis.setOnClickListener {
            if (currentCatId == null) {
                Toast.makeText(requireContext(), "请先添加猫咪", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showDietReportTypeDialog()
        }
    }

    private fun showDietReportTypeDialog() {
        val options = arrayOf("本周报告", "本月报告")
        AlertDialog.Builder(requireContext())
            .setTitle("选择报告类型")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> generateDietReport(7)
                    1 -> generateDietReport(30)
                }
            }
            .show()
    }

    private fun generateDietReport(days: Int) {
        val cat = cats.find { it.id == currentCatId } ?: return
        val allRecords = viewModel.records.value ?: emptyList()

        // 筛选时间范围内的记录
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val filteredRecords = allRecords.filter { it.timestamp >= cutoffTime }

        if (filteredRecords.isEmpty()) {
            Toast.makeText(requireContext(), "该时间段内没有饮食记录", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "正在生成报告...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val aiAdvice = getAIDietAdvice(cat, filteredRecords, days)

                val bitmap = ReportGenerator.generateDietReport(
                    requireContext(),
                    cat,
                    filteredRecords,
                    aiAdvice
                )

                // 保存到临时文件
                val fileName = "饮食报告_${cat.name}_${System.currentTimeMillis()}.png"
                val filePath = ReportGenerator.saveBitmapToTempFile(requireContext(), bitmap, fileName)

                if (filePath != null) {
                    // 打开预览页面
                    val intent = ReportPreviewActivity.newIntent(requireContext(), filePath, fileName)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "报告生成失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "生成报告失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getAIDietAdvice(cat: Cat, records: List<DietRecord>, days: Int): String {
        val waterCount = records.count { it.type == DietRecord.TYPE_WATER }
        val foodCount = records.count { it.type == DietRecord.TYPE_FOOD }
        val snackCount = records.count { it.type == DietRecord.TYPE_SNACK }
        val treatCount = records.count { it.type == DietRecord.TYPE_TREAT }

        val prompt = buildString {
            append("作为猫咪营养专家，请分析以下猫咪的饮食状况并给出建议：\n\n")
            append("猫咪名字：${cat.name}\n")
            append("猫咪品种：${cat.breed}\n")
            append("分析时间段：最近${days}天\n\n")

            append("饮食统计：\n")
            append("- 喝水：${waterCount}次\n")
            append("- 猫粮：${foodCount}次\n")
            append("- 零食：${snackCount}次\n")
            append("- 猫条：${treatCount}次\n")
            append("- 总计：${records.size}次\n\n")

            append("请给出简洁的营养均衡建议，控制在100字以内。")
        }

        return try {
            val request = ChatRequest(message = prompt)
            val response = RetrofitClient.apiService.chat(request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.response ?: "暂无建议"
            } else {
                "建议保持饮食均衡，多让猫咪喝水。零食和猫条要适量，避免影响正餐食欲。"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "建议保持饮食均衡，多让猫咪喝水。零食和猫条要适量，避免影响正餐食欲。"
        }
    }

    private fun updateTabVisibility() {
        if (currentTab == 0) {
            // 饮食记录
            binding.rvDietRecords.visibility = View.VISIBLE
            binding.rvReminders.visibility = View.GONE
            binding.llEmptyDiet.visibility = if (viewModel.records.value.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.llEmptyReminder.visibility = View.GONE
        } else {
            // 喂食提醒
            binding.rvDietRecords.visibility = View.GONE
            binding.rvReminders.visibility = View.VISIBLE
            binding.llEmptyDiet.visibility = View.GONE
            binding.llEmptyReminder.visibility = if (viewModel.reminders.value.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showRecordOptionsDialog(record: DietRecord) {
        val options = arrayOf("编辑", "删除")

        AlertDialog.Builder(requireContext())
            .setTitle("选择操作")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openEditRecordActivity(record)
                    1 -> confirmDelete(record)
                }
            }
            .show()
    }

    private fun openAddRecordActivity() {
        val intent = Intent(requireContext(), AddDietRecordActivity::class.java)
        currentCatId?.let {
            intent.putExtra("cat_id", it)
        }
        startActivity(intent)
    }

    private fun openEditRecordActivity(record: DietRecord) {
        val intent = Intent(requireContext(), AddDietRecordActivity::class.java)
        intent.putExtra("record_id", record.id)
        intent.putExtra("cat_id", record.catId)
        startActivity(intent)
    }

    private fun confirmDelete(record: DietRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这条饮食记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteDietRecord(record) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun openAddReminderActivity() {
        val intent = Intent(requireContext(), AddReminderActivity::class.java)
        currentCatId?.let { catId ->
            intent.putExtra("cat_id", catId)
            val catName = cats.find { it.id == catId }?.name ?: ""
            intent.putExtra("cat_name", catName)
        }
        reminderActivityLauncher.launch(intent)
    }

    private fun openEditReminderActivity(reminder: FeedingReminder) {
        val intent = Intent(requireContext(), AddReminderActivity::class.java)
        intent.putExtra("reminder_id", reminder.id)
        intent.putExtra("cat_id", reminder.catId)
        val catName = cats.find { it.id == reminder.catId }?.name ?: ""
        intent.putExtra("cat_name", catName)
        reminderActivityLauncher.launch(intent)
    }

    private fun confirmDeleteReminder(reminder: FeedingReminder) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这条喂食提醒吗？")
            .setPositiveButton("删除") { _, _ ->
                // 先取消闹钟
                ReminderScheduler.cancelReminder(requireContext(), reminder)
                viewModel.deleteReminder(reminder) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "删除成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // 刷新列表
        refreshData()
    }

    private fun refreshData() {
        currentCatId?.let {
            viewModel.loadDietRecords(it)
            // 刷新提醒列表（从本地数据库加载，确保显示最新的更改）
            viewModel.loadRemindersFromLocal(it)
        }
    }

    private fun scheduleReminders(reminders: List<FeedingReminder>) {
        reminders.forEach { reminder ->
            if (reminder.enabled) {
                val catName = cats.find { it.id == reminder.catId }?.name ?: ""
                ReminderScheduler.scheduleReminder(requireContext(), reminder, catName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
