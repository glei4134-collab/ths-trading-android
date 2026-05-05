package com.ths.tradingai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class NotificationFragment : Fragment() {
    private lateinit var notifList: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notifList = view.findViewById(R.id.notifList)
        notifList.layoutManager = LinearLayoutManager(requireContext())
        loadNotifications()
    }

    private fun loadNotifications() {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = ApiClient.getNotifications(server, token)
                withContext(Dispatchers.Main) {
                    if (result.success && result.data != null) {
                        notifList.adapter = NotifAdapter(result.data)
                    }
                }
            } catch (e: Exception) {}
        }
    }
}

class NotifAdapter(private val items: List<ApiClient.Notification>) :
    RecyclerView.Adapter<NotifAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.notifTitle)
        val message: TextView = view.findViewById(R.id.notifMessage)
        val time: TextView = view.findViewById(R.id.notifTime)
        val card: View = view.findViewById(R.id.notifCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val n = items[position]
        holder.title.text = n.title
        holder.message.text = n.message
        holder.time.text = n.createdAt
        val borderColor = when (n.level) {
            "success" -> 0xFF3fb950.toInt()
            "warning" -> 0xFFd29922.toInt()
            "error" -> 0xFFf85149.toInt()
            else -> 0xFF667eea.toInt()
        }
        holder.card.setBackgroundColor(borderColor)
    }

    override fun getItemCount() = items.size
}
