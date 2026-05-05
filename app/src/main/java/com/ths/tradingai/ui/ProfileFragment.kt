package com.ths.tradingai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ths.tradingai.R
import com.ths.tradingai.util.TokenManager

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.deviceName).text =
            TokenManager.getDeviceName(requireContext()) ?: "未命名"
        view.findViewById<TextView>(R.id.serverUrl).text =
            TokenManager.getServer(requireContext()) ?: "未配置"
    }
}
