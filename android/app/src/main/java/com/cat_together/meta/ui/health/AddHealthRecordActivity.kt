package com.cat_together.meta.ui.health

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityAddHealthRecordBinding
import com.cat_together.meta.model.HealthRecord
import com.cat_together.meta.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHealthRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHealthRecordBinding
    private lateinit var viewModel: HealthViewModel
    private var recordId: String? = null
    private var catId: String? = null
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHealthRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HealthViewModel::class.java]

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
        binding.tvTitle.text = if (recordId != null) "编辑健康记录" else "添加健康记录"

        // 设置记录类型选择器
        val types = listOf("体重", "身高", "疫苗", "驱虫")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnRecordType.adapter = adapter

        binding.spnRecordType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateUnitHint(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 设置日期点击
        binding.layoutDate.setOnClickListener {
            showDatePicker()
        }

        // 设置按钮监听
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveRecord()
        }
    }

    private fun updateUnitHint(typeIndex: Int) {
        binding.tvUnit.text = when (typeIndex) {
            0 -> "kg"
            1 -> "cm"
            else -> ""
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                binding.tvDate.text = DateUtils.formatDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun loadData() {
        if (recordId != null) {
            viewModel.loadHealthRecord(recordId!!) { record ->
                record?.let {
                    // 填充数据
                    binding.spnRecordType.setSelection(getTypeIndex(it.recordType))
                    binding.etValue.setText(it.value.toString())
                    binding.tvDate.text = DateUtils.formatDate(it.recordDate)
                    selectedDate = it.recordDate
                    binding.etNote.setText(it.note)
                }
            }
        } else {
            // 新记录，设置默认日期
            binding.tvDate.text = DateUtils.formatDate(selectedDate)
        }
    }

    private fun saveRecord() {
        val typeIndex = binding.spnRecordType.selectedItemPosition
        val valueStr = binding.etValue.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        // 验证输入
        if (typeIndex < 0) {
            Toast.makeText(this, "请选择记录类型", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(valueStr)) {
            Toast.makeText(this, "请输入数值", Toast.LENGTH_SHORT).show()
            binding.etValue.requestFocus()
            return
        }

        val value = valueStr.toFloatOrNull()
        if (value == null) {
            Toast.makeText(this, "请输入有效的数值", Toast.LENGTH_SHORT).show()
            binding.etValue.requestFocus()
            return
        }

        val recordType = getTypeValue(typeIndex)

        val record = HealthRecord(
            id = recordId ?: "",
            catId = catId!!,
            recordType = recordType,
            value = value,
            recordDate = selectedDate,
            note = note,
            createdAt = if (recordId != null) System.currentTimeMillis() else 0L
        )

        viewModel.saveHealthRecord(record) { success ->
            if (success) {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTypeIndex(type: Int): Int {
        return when (type) {
            HealthRecord.TYPE_WEIGHT -> 0
            HealthRecord.TYPE_HEIGHT -> 1
            HealthRecord.TYPE_VACCINE -> 2
            HealthRecord.TYPE_DEWORMING -> 3
            else -> 0
        }
    }

    private fun getTypeValue(index: Int): Int {
        return when (index) {
            0 -> HealthRecord.TYPE_WEIGHT
            1 -> HealthRecord.TYPE_HEIGHT
            2 -> HealthRecord.TYPE_VACCINE
            3 -> HealthRecord.TYPE_DEWORMING
            else -> HealthRecord.TYPE_WEIGHT
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
