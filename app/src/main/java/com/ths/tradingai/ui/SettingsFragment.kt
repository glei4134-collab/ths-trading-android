package com.ths.tradingai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ths.tradingai.R
import com.ths.tradingai.network.ApiClient
import com.ths.tradingai.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSettings(view)
    }

    private fun loadSettings(view: View) {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val status = ApiClient.getStatus(server, token)
                withContext(Dispatchers.Main) {
                    if (status.success && status.data != null) {
                        view.findViewById<TextView>(R.id.schedulerStatus).text =
                            if (status.data.schedulerEnabled) "运行中" else "已停止"
                        view.findViewById<TextView>(R.id.autoTradeStatus).text =
                            if (status.data.autoTrade) "开启" else "关闭"
                        view.findViewById<TextView>(R.id.connectionStatus).text =
                            if (status.data.connected) "已连接" else "未连接"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.findViewById<TextView>(R.id.schedulerStatus).text = "加载失败"
                }
            }
        }
    }
}
