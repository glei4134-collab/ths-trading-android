package com.ths.tradingai.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
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

        // 请求通知权限
        requestNotificationPermission()

        loadSettings(view)

        view.findViewById<MaterialButton>(R.id.btnRefresh).setOnClickListener { loadSettings(view) }
        view.findViewById<MaterialButton>(R.id.btnToggleScheduler).setOnClickListener { toggleScheduler(view) }
        view.findViewById<MaterialButton>(R.id.btnToggleAutoTrade).setOnClickListener { toggleAutoTrade(view) }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun loadSettings(view: View) {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val status = ApiClient.getStatus(server, token)
                withContext(Dispatchers.Main) {
                    if (status.success && status.data != null) {
                        val s = status.data
                        view.findViewById<TextView>(R.id.connectionStatus).text =
                            if (s.connected) "已连接" else "未连接"
                        view.findViewById<TextView>(R.id.schedulerStatus).text =
                            if (s.schedulerEnabled) "运行中" else "已停止"
                        view.findViewById<TextView>(R.id.autoTradeStatus).text =
                            if (s.autoTrade) "开启" else "关闭"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.findViewById<TextView>(R.id.connectionStatus).text = "加载失败"
                }
            }
        }
    }

    private fun toggleScheduler(view: View) {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = ApiClient.toggleScheduler(server, token)
                withContext(Dispatchers.Main) {
                    if (result.success) {
                        Toast.makeText(requireContext(), "调度器已切换", Toast.LENGTH_SHORT).show()
                        loadSettings(view)
                    } else {
                        Toast.makeText(requireContext(), "操作失败: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "网络错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleAutoTrade(view: View) {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = ApiClient.toggleAutoTrade(server, token)
                withContext(Dispatchers.Main) {
                    if (result.success) {
                        Toast.makeText(requireContext(), "自动交易已切换", Toast.LENGTH_SHORT).show()
                        loadSettings(view)
                    } else {
                        Toast.makeText(requireContext(), "操作失败: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "网络错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
