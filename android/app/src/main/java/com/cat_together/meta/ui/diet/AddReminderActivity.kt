package com.cat_together.meta.ui.diet

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityAddReminderBinding
import com.cat_together.meta.model.FeedingReminder
import com.cat_together.meta.utils.ReminderScheduler
import java.util.Calendar

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var viewModel: DietViewModel
    private var reminderId: String? = null
    private var catId: String? = null
    private var catName: String = ""
    private var selectedHour = 8
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DietViewModel::class.java]

        reminderId = intent.getStringExtra("reminder_id")
        catId = intent.getStringExtra("cat_id")
        catName = intent.getStringExtra("cat_name") ?: ""

        if (catId == null) {
            finish()
            return
        }

        setupViews()
        loadData()
    }

    private fun setupViews() {
        binding.tvTitle.text = if (reminderId != null) "编辑喂食提醒" else "添加喂食提醒"

        val types = listOf("喝水", "猫粮", "零食", "猫条")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnType.adapter = adapter

        val repeats = listOf("每天", "每周", "每月")
        val repeatAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repeats)
        repeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRepeat.adapter = repeatAdapter

        binding.layoutTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveReminder()
        }
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                selectedHour = hourOfDay
                selectedMinute = minute
                binding.tvTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
            },
            selectedHour,
            selectedMinute,
            true
        )
        timePickerDialog.show()
    }

    private fun loadData() {
        if (reminderId != null) {
            viewModel.loadReminder(reminderId!!) { reminder ->
                reminder?.let {
                    binding.spnType.setSelection(getTypeIndex(it.type))
                    selectedHour = it.time.substring(0, 2).toInt()
                    selectedMinute = it.time.substring(3, 5).toInt()
                    binding.tvTime.text = it.time
                    binding.spnRepeat.setSelection(getRepeatIndex(it.repeatRule))
                    binding.switchEnabled.isChecked = it.enabled
                }
            }
        } else {
            binding.tvTime.text = String.format("%02d:%02d", selectedHour, selectedMinute)
        }
    }

    private fun saveReminder() {
        val typeIndex = binding.spnType.selectedItemPosition
        val repeatIndex = binding.spnRepeat.selectedItemPosition
        val enabled = binding.switchEnabled.isChecked

        val time = String.format("%02d:%02d", selectedHour, selectedMinute)
        val type = getTypeValue(typeIndex)
        val repeat = getRepeatValue(repeatIndex)

        val reminder = FeedingReminder(
            id = reminderId ?: "",
            catId = catId!!,
            type = type,
            time = time,
            repeatRule = repeat,
            enabled = enabled,
            createdAt = if (reminderId != null) System.currentTimeMillis() else 0L
        )

        viewModel.saveReminder(reminder) { success, createdReminder ->
            if (success) {
                // 使用服务器返回的提醒（包含正确的ID）来调度
                val reminderToSchedule = createdReminder ?: reminder
                // 保存成功后立即调度提醒
                if (enabled) {
                    ReminderScheduler.scheduleReminder(this@AddReminderActivity, reminderToSchedule, catName)
                }
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTypeIndex(type: Int): Int {
        return when (type) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 3
            else -> 0
        }
    }

    private fun getTypeValue(index: Int): Int {
        return when (index) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            else -> 1
        }
    }

    private fun getRepeatIndex(rule: String): Int {
        return when (rule) {
            FeedingReminder.REPEAT_DAILY -> 0
            FeedingReminder.REPEAT_WEEKLY -> 1
            FeedingReminder.REPEAT_MONTHLY -> 2
            else -> 0
        }
    }

    private fun getRepeatValue(index: Int): String {
        return when (index) {
            0 -> FeedingReminder.REPEAT_DAILY
            1 -> FeedingReminder.REPEAT_WEEKLY
            2 -> FeedingReminder.REPEAT_MONTHLY
            else -> FeedingReminder.REPEAT_DAILY
        }
    }
}
