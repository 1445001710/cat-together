package com.cat_together.meta.ui.diet

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityAddDietRecordBinding
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.model.FeedingReminder
import com.cat_together.meta.utils.DateUtils
import java.util.Calendar

class AddDietRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDietRecordBinding
    private lateinit var viewModel: DietViewModel
    private var recordId: String? = null
    private var catId: String? = null
    private var selectedDate: Long = System.currentTimeMillis()
    private var reminderTime: String = "08:00"
    private var isReminderEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDietRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DietViewModel::class.java]

        recordId = intent.getStringExtra("record_id")
        catId = intent.getStringExtra("cat_id")

        if (catId == null) {
            finish()
            return
        }

        setupViews()
        loadData()
    }

    private fun setupViews() {
        // 设置标题
        binding.tvTitle.text = if (recordId != null) "编辑饮食记录" else "添加饮食记录"

        // 设置类型选择器
        val types = listOf("喂水", "喂食", "零食", "猫条")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnType.adapter = adapter

        binding.spnType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateTypeUI(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 设置提醒切换
        binding.swReminder.setOnCheckedChangeListener { _, isChecked ->
            isReminderEnabled = isChecked
            binding.layoutReminder.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // 设置时间点击
        binding.layoutTime.setOnClickListener {
            showTimePicker()
        }

        // 设置重复规则选择器
        val repeatRules = listOf("每天", "仅一次")
        val repeatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repeatRules)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRepeatRule.adapter = repeatAdapter

        // 设置按钮监听
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveRecord()
        }

        // 初始化UI
        updateTypeUI(0)
    }

    private fun updateTypeUI(typeIndex: Int) {
        val isFood = typeIndex == 1
        binding.layoutReminderContainer.visibility = if (isFood) View.VISIBLE else View.GONE
        binding.layoutAmount.visibility = if (typeIndex != 0) View.VISIBLE else View.GONE

        // 更新单位提示
        binding.tvUnit.text = when (typeIndex) {
            1 -> "克"
            else -> "次"
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = reminderTime.substringBefore(":").toIntOrNull() ?: 8
        val minute = reminderTime.substringAfter(":").toIntOrNull() ?: 0

        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                reminderTime = String.format("%02d:%02d", hourOfDay, minute)
                binding.tvTime.text = reminderTime
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun loadData() {
        if (recordId != null) {
            viewModel.loadDietRecord(recordId!!) { record ->
                record?.let {
                    // 填充数据
                    binding.spnType.setSelection(getTypeIndex(it.type))
                    binding.etAmount.setText(it.amount.toString())
                    binding.etNote.setText(it.note)
                }
            }
        } else {
            // 查询是否已有提醒设置
            viewModel.loadReminder(catId!!) { reminder ->
                reminder?.let {
                    isReminderEnabled = true
                    reminderTime = it.time
                    binding.swReminder.isChecked = true
                    binding.layoutReminder.visibility = View.VISIBLE
                    binding.tvTime.text = it.time
                    binding.spnRepeatRule.setSelection(if (it.repeatRule == "daily") 0 else 1)
                }
            }
        }
    }

    private fun saveRecord() {
        val typeIndex = binding.spnType.selectedItemPosition
        val amountStr = binding.etAmount.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        // 验证输入
        if (typeIndex < 0) {
            Toast.makeText(this, "请选择类型", Toast.LENGTH_SHORT).show()
            return
        }

        val type = getTypeValue(typeIndex)

        val record = DietRecord(
            id = recordId ?: "",
            catId = catId!!,
            type = type,
            amount = amountStr.toIntOrNull() ?: 1,
            timestamp = System.currentTimeMillis(),
            note = note,
            createdAt = if (recordId != null) System.currentTimeMillis() else 0L
        )

        viewModel.saveDietRecord(record) { success ->
            if (success) {
                // 保存提醒（如果是喂食）
                if (type == DietRecord.TYPE_FOOD && isReminderEnabled) {
                    saveReminder()
                }
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveReminder() {
        val repeatRule = when (binding.spnRepeatRule.selectedItemPosition) {
            0 -> "daily"
            else -> "once"
        }

        val reminder = FeedingReminder(
            id = "",
            catId = catId!!,
            type = DietRecord.TYPE_FOOD,
            time = reminderTime,
            repeatRule = repeatRule,
            enabled = true,
            createdAt = System.currentTimeMillis()
        )

        viewModel.saveReminder(reminder) { _, _ -> }
    }

    private fun getTypeIndex(type: Int): Int {
        return when (type) {
            DietRecord.TYPE_WATER -> 0
            DietRecord.TYPE_FOOD -> 1
            DietRecord.TYPE_SNACK -> 2
            DietRecord.TYPE_TREAT -> 3
            else -> 0
        }
    }

    private fun getTypeValue(index: Int): Int {
        return when (index) {
            0 -> DietRecord.TYPE_WATER
            1 -> DietRecord.TYPE_FOOD
            2 -> DietRecord.TYPE_SNACK
            3 -> DietRecord.TYPE_TREAT
            else -> DietRecord.TYPE_FOOD
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
