package com.ths.tradingai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ths.tradingai.R
import com.ths.tradingai.network.ApiClient
import com.ths.tradingai.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BindFragment : Fragment() {

    private lateinit var bindCodeInput: EditText
    private lateinit var deviceNameInput: EditText
    private lateinit var bindButton: Button
    private lateinit var errorText: TextView
    private lateinit var serverInput: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bind, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindCodeInput = view.findViewById(R.id.bindCodeInput)
        deviceNameInput = view.findViewById(R.id.deviceNameInput)
        bindButton = view.findViewById(R.id.bindButton)
        errorText = view.findViewById(R.id.errorText)
        serverInput = view.findViewById(R.id.serverInput)

        // 加载已保存的服务器地址
        val savedServer = TokenManager.getServer(requireContext())
        if (!savedServer.isNullOrEmpty()) {
            serverInput.setText(savedServer)
        }

        bindButton.setOnClickListener { doBind() }
    }

    private fun doBind() {
        val code = bindCodeInput.text.toString().trim()
        val name = deviceNameInput.text.toString().trim().ifEmpty { "我的手机" }
        val server = serverInput.text.toString().trim()

        if (code.length != 6) {
            errorText.text = "请输入6位数字绑定码"
            errorText.visibility = View.VISIBLE
            return
        }

        if (server.isEmpty()) {
            errorText.text = "请输入服务器地址"
            errorText.visibility = View.VISIBLE
            return
        }

        // 保存服务器地址
        TokenManager.saveServer(requireContext(), server)

        bindButton.isEnabled = false
        bindButton.text = "绑定中..."
        errorText.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = ApiClient.claimBindCode(server, code, name)
                withContext(Dispatchers.Main) {
                    if (result.success && result.token != null) {
                        TokenManager.saveToken(requireContext(), result.token)
                        result.deviceId?.let { TokenManager.saveDeviceId(requireContext(), it) }
                        TokenManager.saveDeviceName(requireContext(), name)
                        Toast.makeText(requireContext(), "绑定成功！", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.accountFragment)
                    } else {
                        errorText.text = result.message ?: "绑定失败"
                        errorText.visibility = View.VISIBLE
                        bindButton.isEnabled = true
                        bindButton.text = "绑定"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorText.text = "网络错误: ${e.message}"
                    errorText.visibility = View.VISIBLE
                    bindButton.isEnabled = true
                    bindButton.text = "绑定"
                }
            }
        }
    }
}
