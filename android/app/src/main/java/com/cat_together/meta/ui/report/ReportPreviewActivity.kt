package com.cat_together.meta.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cat_together.meta.databinding.ActivityReportPreviewBinding
import com.cat_together.meta.utils.ReportGenerator

class ReportPreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportPreviewBinding
    private var filePath: String = ""
    private var fileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getStringExtra("filePath") ?: ""
        fileName = intent.getStringExtra("fileName") ?: "report.png"

        if (filePath.isEmpty()) {
            Toast.makeText(this, "报告加载失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // 加载并显示预览图
        val bitmap = ReportGenerator.loadBitmapFromFile(filePath)
        if (bitmap != null) {
            binding.ivPreview.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "报告加载失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 下载按钮
        binding.btnDownload.setOnClickListener {
            bitmap?.let { bmp ->
                val saved = ReportGenerator.saveBitmapToGallery(this, bmp, fileName)
                if (saved) {
                    Toast.makeText(this, "已保存到相册", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 关闭按钮
        binding.btnClose.setOnClickListener {
            // 删除临时文件
            ReportGenerator.deleteTempFile(filePath)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理临时文件
        if (filePath.isNotEmpty()) {
            ReportGenerator.deleteTempFile(filePath)
        }
    }

    companion object {
        fun newIntent(context: Context, filePath: String, fileName: String): Intent {
            return Intent(context, ReportPreviewActivity::class.java).apply {
                putExtra("filePath", filePath)
                putExtra("fileName", fileName)
            }
        }
    }
}
