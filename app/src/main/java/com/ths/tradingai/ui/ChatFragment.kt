package com.ths.tradingai.ui

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

    data class ChatMessage(val role: String, val content: String, val time: String)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatMessages = view.findViewById(R.id.chatMessages)
        chatInput = view.findViewById(R.id.chatInput)
        sendButton = view.findViewById(R.id.sendButton)

        chatMessages.layoutManager = LinearLayoutManager(requireContext())
        adapter = ChatAdapter(messages)
        chatMessages.adapter = adapter

        // 欢迎消息
        addMessage("bot", "你好！我是AI交易助手。可以问我股票分析、市场行情，或输入交易指令。")

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
