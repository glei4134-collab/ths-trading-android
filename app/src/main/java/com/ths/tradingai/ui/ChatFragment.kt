package com.ths.tradingai.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ths.tradingai.R
import com.ths.tradingai.network.ApiClient
import com.ths.tradingai.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {

    private lateinit var chatMessages: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var sendButton: ImageButton
    private val messages = mutableListOf<ChatMessage>()
    private val chatHistory = mutableListOf<Map<String, String>>()
    private lateinit var adapter: ChatAdapter
    private val gson = Gson()

    data class ChatMessage(val role: String, val content: String, val time: String)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatMessages = view.findViewById(R.id.chatMessages)
        chatInput = view.findViewById(R.id.chatInput)
        sendButton = view.findViewById(R.id.sendButton)

        adapter = ChatAdapter(messages)
        chatMessages.layoutManager = LinearLayoutManager(requireContext())
        chatMessages.adapter = adapter

        // 加载本地聊天记录
        loadHistory()

        // 如果没有历史记录，添加欢迎消息
        if (messages.isEmpty()) {
            addMessage("bot", "你好！我是AI交易助手。可以问我股票分析、市场行情，或输入交易指令。")
        }

        sendButton.setOnClickListener { sendChat() }
    }

    private fun sendChat() {
        val msg = chatInput.text.toString().trim()
        if (msg.isEmpty()) return

        addMessage("user", msg)
        chatInput.text.clear()
        sendButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val server = TokenManager.getServer(requireContext()) ?: return@launch
                val token = TokenManager.getToken(requireContext()) ?: return@launch
                val result = ApiClient.chat(server, token, msg, chatHistory)
                withContext(Dispatchers.Main) {
                    sendButton.isEnabled = true
                    if (result.success && result.data != null) {
                        val reply = result.data.content
                        addMessage("bot", reply)
                        chatHistory.add(mapOf("role" to "user", "content" to msg))
                        chatHistory.add(mapOf("role" to "assistant", "content" to reply))
                        if (chatHistory.size > 40) {
                            chatHistory.subList(0, chatHistory.size - 40).clear()
                        }
                        saveHistory()
                    } else {
                        addMessage("bot", "❌ ${result.message ?: "请求失败"}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sendButton.isEnabled = true
                    addMessage("bot", "❌ 网络错误: ${e.message}")
                }
            }
        }
    }

    private fun addMessage(role: String, content: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages.add(ChatMessage(role, content, time))
        adapter.notifyItemInserted(messages.size - 1)
        chatMessages.scrollToPosition(messages.size - 1)
    }

    private fun saveHistory() {
        try {
            val prefs = requireContext().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("messages", gson.toJson(messages))
                .putString("history", gson.toJson(chatHistory))
                .apply()
        } catch (e: Exception) {}
    }

    private fun loadHistory() {
        try {
            val prefs = requireContext().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)
            val msgJson = prefs.getString("messages", null)
            val histJson = prefs.getString("history", null)
            if (msgJson != null) {
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val saved: List<ChatMessage> = gson.fromJson(msgJson, type)
                messages.addAll(saved)
                adapter.notifyDataSetChanged()
            }
            if (histJson != null) {
                val type = object : TypeToken<List<Map<String, String>>>() {}.type
                val saved: List<Map<String, String>> = gson.fromJson(histJson, type)
                chatHistory.addAll(saved)
            }
        } catch (e: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveHistory()
    }
}

class ChatAdapter(private val messages: List<ChatFragment.ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.chatContent)
        val time: TextView = view.findViewById(R.id.chatTime)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 1) R.layout.item_chat_user else R.layout.item_chat_bot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]
        holder.content.text = msg.content
        holder.time.text = msg.time
    }

    override fun getItemCount() = messages.size
}
