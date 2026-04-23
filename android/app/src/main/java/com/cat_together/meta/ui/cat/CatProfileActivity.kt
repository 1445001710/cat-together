package com.cat_together.meta.ui.cat

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityCatProfileBinding
import com.cat_together.meta.model.Cat
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.DateUtils
import com.cat_together.meta.utils.SharedPreferencesHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CatProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCatProfileBinding
    private val viewModel: CatProfileViewModel by lazy {
        ViewModelProvider(this)[CatProfileViewModel::class.java]
    }
    private val prefs by lazy { SharedPreferencesHelper.getInstance(this) }

    private var catId: String? = null
    private var birthdayTimestamp: Long = 0L
    private var localAvatarPath: String? = null  // 本地选中的图片路径
    private var serverAvatarUrl: String? = null  // 上传到服务器后的URL

    // 图片选择器 - 只选择不立即上传
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 如果选择的是同一张图片（内容URI相同），跳过
            if (localAvatarPath == it.toString()) return@let
            // 保存到本地变量，只做预览，不上传
            localAvatarPath = it.toString()
            // 显示预览
            binding.ivCatAvatar.setImageURI(it)
        }
    }

    // 默认头像资源
    private val defaultAvatarRes = R.drawable.default_cat_avatar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadCatData()
    }

    private fun setupViews() {
        // 设置 Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // 设置返回键为白色
        binding.toolbar.navigationIcon?.setTint(getColor(R.color.white))
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 头像点击事件
        binding.ivCatAvatar.setOnClickListener {
            showAvatarOptionsDialog()
        }
        binding.tvUploadAvatar.setOnClickListener {
            showAvatarOptionsDialog()
        }

        binding.tvBirthday.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveCat()
        }

        binding.btnDelete.setOnClickListener {
            deleteCat()
        }
    }

    private fun loadCatData() {
        catId = intent.getStringExtra("cat_id")

        if (catId != null) {
            // 编辑模式
            supportActionBar?.title = getString(R.string.cat_profile)
            binding.btnDelete.visibility = android.view.View.VISIBLE

            lifecycleScope.launch {
                val cat = viewModel.getCatById(catId!!)
                cat?.let {
                    binding.etCatName.setText(it.name)
                    binding.etBreed.setText(it.breed)
                    binding.etColor.setText(it.color)

                    when (it.gender) {
                        Cat.GENDER_MALE -> binding.rbMale.isChecked = true
                        Cat.GENDER_FEMALE -> binding.rbFemale.isChecked = true
                        else -> binding.rgGender.clearCheck()
                    }

                    birthdayTimestamp = it.birthday
                    if (birthdayTimestamp > 0) {
                        binding.tvBirthday.text = DateUtils.formatDate(birthdayTimestamp)
                    }

                    binding.etWeight.setText(if (it.weight > 0) it.weight.toString() else "")
                    binding.etHeight.setText(if (it.height > 0) it.height.toString() else "")

                    if (!it.avatar.isNullOrEmpty()) {
                        serverAvatarUrl = it.avatar
                        Glide.with(this@CatProfileActivity)
                            .load(it.avatar)
                            .placeholder(defaultAvatarRes)
                            .error(defaultAvatarRes)
                            .circleCrop()
                            .into(binding.ivCatAvatar)
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        val dpd = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)
                birthdayTimestamp = calendar.timeInMillis
                binding.tvBirthday.text = DateUtils.formatDate(birthdayTimestamp)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        )
        dpd.show()
    }

    private fun showAvatarOptionsDialog() {
        val options = arrayOf("本地上传", "使用默认头像")
        AlertDialog.Builder(this)
            .setTitle("设置头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> {
                        localAvatarPath = null
                        serverAvatarUrl = null
                        binding.ivCatAvatar.setImageResource(defaultAvatarRes)
                    }
                }
            }
            .show()
    }

    private fun uploadAvatar(imagePath: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@CatProfileActivity, "正在上传...", Toast.LENGTH_SHORT).show()

                // 从Content URI获取输入流并创建临时文件
                val uri = android.net.Uri.parse(imagePath)
                val inputStream = contentResolver.openInputStream(uri)
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                // 根据MIME类型确定扩展名
                val extension = if (mimeType.contains("png")) ".png"
                               else if (mimeType.contains("gif")) ".gif"
                               else if (mimeType.contains("webp")) ".webp"
                               else ".jpg"
                val tempFile = java.io.File(cacheDir, "avatar_${System.currentTimeMillis()}$extension")
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // 创建RequestBody，直接从文件
                val mediaType = mimeType.toMediaTypeOrNull() ?: return@launch
                val fileBody = RequestBody.create(mediaType, tempFile)
                val part = MultipartBody.Part.createFormData("avatar", tempFile.name, fileBody)

                val response = RetrofitClient.apiService.uploadAvatar(part)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    serverAvatarUrl = response.body()?.data?.avatar
                    Toast.makeText(this@CatProfileActivity, "上传成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CatProfileActivity, "上传失败", Toast.LENGTH_SHORT).show()
                }

                // 清理临时文件
                tempFile.delete()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CatProfileActivity, "上传失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 同步上传版本（用于保存按钮触发）
    private suspend fun uploadAvatarSync(imagePath: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = android.net.Uri.parse(imagePath)
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null

                // 从输入流解码Bitmap
                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) return@withContext null

                // 压缩图片：最大边限制在800px，质量80%
                val maxSize = 800
                val scale = if (originalBitmap.width > originalBitmap.height) {
                    maxSize.toFloat() / originalBitmap.width
                } else {
                    maxSize.toFloat() / originalBitmap.height
                }
                val scaledBitmap = if (scale < 1) {
                    val newWidth = (originalBitmap.width * scale).toInt()
                    val newHeight = (originalBitmap.height * scale).toInt()
                    android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                } else {
                    originalBitmap
                }

                // 写入压缩后的图片到临时文件
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val extension = if (mimeType.contains("png")) ".png"
                               else if (mimeType.contains("gif")) ".gif"
                               else if (mimeType.contains("webp")) ".webp"
                               else ".jpg"
                val tempFile = java.io.File(cacheDir, "avatar_${System.currentTimeMillis()}$extension")

                val compressFormat = when {
                    extension == ".png" -> android.graphics.Bitmap.CompressFormat.PNG
                    extension == ".webp" -> android.graphics.Bitmap.CompressFormat.WEBP
                    else -> android.graphics.Bitmap.CompressFormat.JPEG
                }

                tempFile.outputStream().use { output ->
                    scaledBitmap.compress(compressFormat, 80, output)
                }

                // 释放Bitmap内存
                if (scaledBitmap != originalBitmap) {
                    scaledBitmap.recycle()
                }
                originalBitmap.recycle()

                // 上传
                val mediaType = mimeType.toMediaTypeOrNull() ?: return@withContext null
                val fileBody = RequestBody.create(mediaType, tempFile)
                val part = MultipartBody.Part.createFormData("avatar", tempFile.name, fileBody)

                val response = RetrofitClient.apiService.uploadAvatar(part)
                tempFile.delete()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.data?.avatar
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun saveCat() {
        val name = binding.etCatName.text.toString().trim()
        val breed = binding.etBreed.text.toString().trim()
        val color = binding.etColor.text.toString().trim()
        val weight = binding.etWeight.text.toString().trim()
        val height = binding.etHeight.text.toString().trim()

        // 验证名字
        if (TextUtils.isEmpty(name)) {
            binding.etCatName.requestFocus()
            Toast.makeText(this, "请输入猫咪名字", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.length > 50) {
            binding.etCatName.requestFocus()
            Toast.makeText(this, "猫咪名字不能超过50个字符", Toast.LENGTH_SHORT).show()
            return
        }

        // 验证体重（可选，但如果有值必须在合理范围）
        val weightValue = weight.toFloatOrNull()
        if (weight.isNotEmpty() && weightValue == null) {
            binding.etWeight.requestFocus()
            Toast.makeText(this, "请输入有效的体重数值", Toast.LENGTH_SHORT).show()
            return
        }
        if (weightValue != null && (weightValue < 0.1f || weightValue > 50f)) {
            binding.etWeight.requestFocus()
            Toast.makeText(this, "体重请输入0.1~50公斤之间的数值", Toast.LENGTH_SHORT).show()
            return
        }

        // 验证身高（可选，但如果有值必须在合理范围）
        val heightValue = height.toFloatOrNull()
        if (height.isNotEmpty() && heightValue == null) {
            binding.etHeight.requestFocus()
            Toast.makeText(this, "请输入有效的身高数值", Toast.LENGTH_SHORT).show()
            return
        }
        if (heightValue != null && (heightValue < 5f || heightValue > 120f)) {
            binding.etHeight.requestFocus()
            Toast.makeText(this, "身高请输入5~120厘米之间的数值", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when {
            binding.rbMale.isChecked -> Cat.GENDER_MALE
            binding.rbFemale.isChecked -> Cat.GENDER_FEMALE
            else -> Cat.GENDER_UNKNOWN
        }

        // 如果有本地选中的新图片，先上传
        if (localAvatarPath != null) {
            lifecycleScope.launch {
                try {
                    // 上传图片到OSS
                    val uploadedUrl = uploadAvatarSync(localAvatarPath!!)
                    if (uploadedUrl != null) {
                        serverAvatarUrl = uploadedUrl
                        saveCatWithAvatar(name, breed, gender, color, weightValue, heightValue)
                    } else {
                        Toast.makeText(this@CatProfileActivity, "图片上传失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CatProfileActivity, "上传失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveCatWithAvatar(name, breed, gender, color, weightValue, heightValue)
        }
    }

    private fun saveCatWithAvatar(
        name: String,
        breed: String,
        gender: Int,
        color: String,
        weightValue: Float?,
        heightValue: Float?
    ) {
        val cat = Cat(
            id = catId ?: "",
            userId = prefs.user?.id ?: "",
            name = name,
            breed = breed,
            gender = gender,
            birthday = birthdayTimestamp,
            color = color,
            avatar = serverAvatarUrl ?: "",
            weight = weightValue ?: 0f,
            height = heightValue ?: 0f
        )

        val isNew = catId == null

        lifecycleScope.launch {
            try {
                val success = viewModel.saveCat(cat, isNew)
                if (success) {
                    Toast.makeText(this@CatProfileActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CatProfileActivity, "保存失败，请重试", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CatProfileActivity, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCat() {
        catId?.let { id ->
            lifecycleScope.launch {
                try {
                    viewModel.deleteCatById(id)
                    Toast.makeText(this@CatProfileActivity, "删除成功", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@CatProfileActivity, "删除失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
