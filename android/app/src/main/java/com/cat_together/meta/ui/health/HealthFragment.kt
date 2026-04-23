package com.cat_together.meta.ui.health

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.cat_together.meta.CatTogetherApp
import com.cat_together.meta.R
import com.cat_together.meta.databinding.FragmentHealthBinding
import com.cat_together.meta.model.Cat
import com.cat_together.meta.model.HealthRecord
import com.cat_together.meta.ui.report.ReportPreviewActivity
import com.cat_together.meta.utils.ReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class HealthFragment : Fragment() {

    private var _binding: FragmentHealthBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HealthViewModel
    private lateinit var healthAdapter: HealthAdapter
    private var currentCatId: String? = null
    private var cats: List<Cat> = emptyList()
    private val catViews = mutableMapOf<String, View>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // 加载猫咪列表
        loadCats()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HealthViewModel::class.java]
    }

    private fun setupRecyclerView() {
        healthAdapter = HealthAdapter { record ->
            showRecordOptionsDialog(record)
        }

        binding.rvHealthRecords.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthAdapter
        }
    }

    private fun setupObservers() {
        viewModel.records.observe(viewLifecycleOwner) { records ->
            if (records == null) return@observe
            if (records.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvHealthRecords.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvHealthRecords.visibility = View.VISIBLE
            }
            healthAdapter.submitList(records)
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
            if (currentCatId == null) {
                Toast.makeText(requireContext(), "请先添加猫咪", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openAddRecordActivity(currentCatId!!)
        }

        // 健康分析VIP按钮
        binding.btnHealthAnalysis.setOnClickListener {
            if (currentCatId == null) {
                Toast.makeText(requireContext(), "请先添加猫咪", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showReportTypeDialog()
        }
    }

    private fun showReportTypeDialog() {
        val options = arrayOf("本周报告", "本月报告")
        AlertDialog.Builder(requireContext())
            .setTitle("选择报告类型")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> generateHealthReport(7)  // 本周
                    1 -> generateHealthReport(30) // 本月
                }
            }
            .show()
    }

    private fun generateHealthReport(days: Int) {
        val cat = cats.find { it.id == currentCatId } ?: return
        val allRecords = viewModel.records.value ?: emptyList()

        // 筛选时间范围内的记录
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val filteredRecords = allRecords.filter { it.recordDate >= cutoffTime }

        if (filteredRecords.isEmpty()) {
            Toast.makeText(requireContext(), "该时间段内没有健康记录", Toast.LENGTH_SHORT).show()
            return
        }

        // 显示加载中
        Toast.makeText(requireContext(), "正在生成报告...", Toast.LENGTH_SHORT).show()

        // 调用AI生成建议
        lifecycleScope.launch {
            try {
                val aiAdvice = getAIHealthAdvice(cat, filteredRecords, days)

                // 生成报告图片
                val bitmap = ReportGenerator.generateHealthReport(
                    requireContext(),
                    cat,
                    filteredRecords,
                    aiAdvice
                )

                // 保存到临时文件
                val fileName = "健康报告_${cat.name}_${System.currentTimeMillis()}.png"
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

    private suspend fun getAIHealthAdvice(cat: Cat, records: List<HealthRecord>, days: Int): String {
        // 构建AI请求
        val weightRecords = records.filter { it.recordType == HealthRecord.TYPE_WEIGHT }
        val heightRecords = records.filter { it.recordType == HealthRecord.TYPE_HEIGHT }
        val vaccineRecords = records.filter { it.recordType == HealthRecord.TYPE_VACCINE }
        val dewormRecords = records.filter { it.recordType == HealthRecord.TYPE_DEWORMING }

        val prompt = buildString {
            append("作为猫咪健康专家，请分析以下猫咪的健康状况并给出建议：\n\n")
            append("猫咪名字：${cat.name}\n")
            append("猫咪品种：${cat.breed}\n")
            append("分析时间段：最近${days}天\n\n")

            if (weightRecords.isNotEmpty()) {
                append("体重记录：\n")
                weightRecords.forEach { record ->
                    append("- ${Date(record.recordDate).toLocaleString()}：${record.value}kg\n")
                }
            }

            if (heightRecords.isNotEmpty()) {
                append("身高记录：\n")
                heightRecords.forEach { record ->
                    append("- ${Date(record.recordDate).toLocaleString()}：${record.value}cm\n")
                }
            }

            if (vaccineRecords.isNotEmpty()) {
                append("疫苗记录：${vaccineRecords.size}次\n")
            }

            if (dewormRecords.isNotEmpty()) {
                append("驱虫记录：${dewormRecords.size}次\n")
            }

            append("\n请给出简洁的健康建议，控制在100字以内。")
        }

        // 调用API
        return try {
            val request = com.cat_together.meta.network.ChatRequest(message = prompt)
            val response = com.cat_together.meta.network.RetrofitClient.apiService.chat(request)
            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data?.response ?: "暂无建议"
            } else {
                "根据记录显示，猫咪健康状况良好，建议继续保持均衡饮食和定期运动。如有异常请及时就医。"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "根据记录显示，猫咪健康状况良好，建议继续保持均衡饮食和定期运动。如有异常请及时就医。"
        }
    }

    private fun loadCats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                cats = CatTogetherApp.database.catDao().getAllCatsList()
                withContext(Dispatchers.Main) {
                    if (cats.isNotEmpty()) {
                        currentCatId = cats[0].id
                        setupCatSelector()
                        viewModel.setCurrentCat(currentCatId!!)
                    } else {
                        Toast.makeText(requireContext(), "请先添加猫咪", Toast.LENGTH_SHORT).show()
                    }
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

            // 加载头像
            Glide.with(ivAvatar)
                .load(cat.avatar?.takeIf { !it.isNullOrEmpty() })
                .placeholder(R.drawable.default_cat_avatar)
                .error(R.drawable.default_cat_avatar)
                .circleCrop()
                .into(ivAvatar)

            // 设置选中状态
            if (cat.id == currentCatId) {
                selectedIndicator.visibility = View.VISIBLE
                tvName.setTextColor(resources.getColor(R.color.primary_pink, null))
            } else {
                selectedIndicator.visibility = View.GONE
                tvName.setTextColor(resources.getColor(R.color.text_secondary, null))
            }

            // 点击事件
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

        // 更新UI选中状态
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

        // 加载该猫咪的健康记录
        viewModel.setCurrentCat(catId)
    }

    private fun showRecordOptionsDialog(record: HealthRecord) {
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

    private fun openAddRecordActivity(catId: String) {
        val intent = Intent(requireContext(), AddHealthRecordActivity::class.java)
        intent.putExtra("cat_id", catId)
        startActivity(intent)
    }

    private fun openEditRecordActivity(record: HealthRecord) {
        val intent = Intent(requireContext(), AddHealthRecordActivity::class.java)
        intent.putExtra("record_id", record.id)
        intent.putExtra("cat_id", record.catId)
        startActivity(intent)
    }

    private fun confirmDelete(record: HealthRecord) {
        AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage("确定要删除这条健康记录吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteHealthRecord(record) { success ->
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
        // 刷新列表（不重新加载猫咪列表，避免currentCatId被重置）
        currentCatId?.let {
            viewModel.loadHealthRecords(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}