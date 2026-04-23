package com.cat_together.meta.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cat_together.meta.R
import com.cat_together.meta.databinding.FragmentChatBinding
import com.cat_together.meta.model.ChatMessage

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // 添加欢迎消息
        viewModel.addMessage(
            ChatMessage(
                id = "welcome",
                content = "你好！我是AI客服小猫，有什么可以帮助你的吗？",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()

        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages)
            binding.rvChat.smoothScrollToPosition(messages.size - 1)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        // 快捷问题
        binding.btnQuestion1.setOnClickListener {
            sendQuickQuestion("我的猫咪不爱吃东西怎么办？")
        }

        binding.btnQuestion2.setOnClickListener {
            sendQuickQuestion("猫咪应该多久驱虫一次？")
        }

        binding.btnQuestion3.setOnClickListener {
            sendQuickQuestion("猫咪绝育需要注意什么？")
        }

        binding.btnQuestion4.setOnClickListener {
            sendQuickQuestion("如何判断猫咪是否健康？")
        }
    }

    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isEmpty()) {
            Toast.makeText(requireContext(), "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.sendUserMessage(message)
        binding.etMessage.text.clear()
    }

    private fun sendQuickQuestion(question: String) {
        viewModel.sendUserMessage(question)
    }
}
