package com.cat_together.meta.ui.ai

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cat_together.meta.model.ChatMessage
import com.cat_together.meta.network.ChatRequest
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.ApiErrorHandler
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun clearError() {
        _error.value = ""
    }

    private val messageList = mutableListOf<ChatMessage>()

    // 本地预设问答
    private val localResponses = mapOf(
        "不吃" to "猫咪不爱吃东西可能有以下原因：\n1. 生病了，观察是否有其他症状\n2. 食物不新鲜或口味不对\n3. 环境压力大或应激反应\n4. 换牙期\n\n建议先观察24小时，如果没有好转建议就医。",
        "食欲" to "猫咪食欲不振可能是因为：\n1. 天气变化或发情期\n2. 食物不符合口味\n3. 精神紧张或受到惊吓\n4. 口腔或消化系统问题\n\n可以尝试更换食物，保持环境安静舒适。如果持续不吃，建议带去看兽医。",
        "驱虫" to "猫咪驱虫建议：\n\n• 室内猫：3-6个月驱虫一次\n• 散养猫：1-3个月驱虫一次\n• 幼猫：建议1月龄开始驱虫\n\n定期驱虫可以预防寄生虫感染，保护猫咪健康。记得选择适合猫咪体重的驱虫药哦~",
        "绝育" to "猫咪绝育注意事项：\n\n1. 年龄建议：6-8个月龄\n2. 术前：禁食8小时，禁水4小时\n3. 术后：佩戴伊丽莎白圈10-14天\n4. 环境清洁：保持干燥安静\n5. 饮食：术后可正常饮食，量要减少\n\n绝育有助于预防生殖系统疾病，延长寿命，还能减少乱叫乱尿的行为~",
        "健康" to "判断猫咪健康的几个要点：\n\n1. 精神状态：活泼好动\n2. 食欲：正常进食\n3. 眼睛：明亮无分泌物\n4. 毛发：光亮无掉毛异常\n5. 粪便：成形、颜色正常\n6. 体重：保持在正常范围\n\n建议定期体检，每年至少一次，早发现早治疗~",
        "洗澡" to "猫咪洗澡小知识：\n\n• 大多数猫咪不需要频繁洗澡，它们自己会清洁\n• 洗澡频率：一般3-6个月一次即可\n• 水温：38-40度左右，温温的\n• 使用专门的猫咪沐浴露\n• 洗完后要完全吹干，避免感冒\n\n注意：如果猫咪特别抗拒洗澡，不要强迫，可能会应激哦~",
        "疫苗" to "猫咪疫苗接种指南：\n\n• 核心疫苗：猫三联（猫瘟、猫鼻支、猫杯状）\n• 首次免疫：8周龄开始，每隔3-4周接种一次，共3针\n• 加强免疫：1年后接种一针\n• 狂犬疫苗：每年接种一次\n\n驱虫和疫苗都很重要，建议选择正规的宠物医院进行~",
        "掉毛" to "猫咪掉毛正常吗？\n\n猫咪掉毛是正常现象，尤其在换季时期。但如果掉毛严重，可能是以下原因：\n\n1. 营养不均衡\n2. 寄生虫或皮肤病\n3. 应激或焦虑\n4. 盐分摄入过多\n\n改善方法：\n• 喂食优质猫粮\n• 定期梳毛\n• 补充维生素和鱼油\n• 保持环境湿润\n\n如果皮肤发红或秃斑，要及时就医哦~",
        "拉肚子" to "猫咪拉肚子怎么办？\n\n先观察情况，轻微拉肚子可以先禁食12-24小时，同时补充水分。\n\n可能原因：\n1. 换粮太突然\n2. 吃了不新鲜的食物\n3. 肠道感染或寄生虫\n4. 应激反应\n\n如果伴随精神差、呕吐、血便等情况，或者持续超过2天，要尽快就医！\n\n饮食上可以喂些易消化的食物，比如煮熟的鸡胸肉~",
        "呕吐" to "猫咪呕吐的原因和处理：\n\n常见原因：\n1. 毛球症（吐毛球是正常的）\n2. 吃太快或太多\n3. 空腹太久\n4. 肠胃炎\n5. 其他疾病\n\n处理方法：\n• 禁食4-6小时观察\n• 提供新鲜清水\n• 少量多餐喂食\n• 喂些益生菌调理肠胃\n\n如果频繁呕吐、吐血、没精神，要立即就医！"
    )

    /**
     * 添加消息
     */
    fun addMessage(message: ChatMessage) {
        messageList.add(message)
        _messages.value = messageList.toList()
    }

    /**
     * 更新最后一条AI消息
     */
    private fun updateLastAiMessage(content: String, isLoading: Boolean = false) {
        val lastIndex = messageList.indexOfLast { !it.isUser }
        if (lastIndex >= 0) {
            messageList[lastIndex] = messageList[lastIndex].copy(content = content, isLoading = isLoading)
            _messages.value = messageList.toList()
        }
    }

    /**
     * 发送用户消息
     */
    fun sendUserMessage(content: String) {
        val userMessage = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            content = content,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )
        addMessage(userMessage)

        // 先检查是否匹配本地问题
        val localAnswer = findLocalResponse(content)
        if (localAnswer != null) {
            // 本地回答
            simulateAiResponse(localAnswer)
        } else {
            // 调用智谱API
            callZhipuApi(content)
        }
    }

    /**
     * 查找本地回复
     */
    private fun findLocalResponse(message: String): String? {
        for ((keyword, answer) in localResponses) {
            if (message.contains(keyword)) {
                return answer
            }
        }
        return null
    }

    /**
     * 模拟AI回复（用于本地问题）
     */
    private fun simulateAiResponse(response: String) {
        viewModelScope.launch {
            // 添加一条带加载状态的AI消息
            val loadingMessage = ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                content = "",
                isUser = false,
                timestamp = System.currentTimeMillis(),
                isLoading = true
            )
            addMessage(loadingMessage)

            // 模拟打字效果
            kotlinx.coroutines.delay(800)

            // 逐字显示
            updateLastAiMessage(response, false)
        }
    }

    /**
     * 调用智谱API
     */
    private fun callZhipuApi(userMessage: String) {
        viewModelScope.launch {
            try {
                // 添加一条带加载状态的AI消息
                val loadingMessage = ChatMessage(
                    id = "ai_${System.currentTimeMillis()}",
                    content = "",
                    isUser = false,
                    timestamp = System.currentTimeMillis(),
                    isLoading = true
                )
                addMessage(loadingMessage)

                val request = ChatRequest(
                    message = userMessage,
                    history = null
                )

                val response = RetrofitClient.apiService.chat(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val reply = response.body()?.data?.response ?: "抱歉，我现在有点忙，稍后再回复你哦~"
                    // 模拟打字效果
                    kotlinx.coroutines.delay(500)
                    updateLastAiMessage(reply, false)
                } else {
                    _error.value = ApiErrorHandler.getMessage(Exception(response.body()?.message ?: "网络开小差了"), "网络开小差了，请稍后再试试~")
                    updateLastAiMessage("网络开小差了，请稍后再试试~", false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = ApiErrorHandler.getMessage(e, "出了点小问题")
                updateLastAiMessage("哎呀，出了点小问题，请稍后再试试~", false)
            }
        }
    }
}