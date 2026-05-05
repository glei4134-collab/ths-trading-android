package com.ths.tradingai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ths.tradingai.R
import com.ths.tradingai.network.ApiClient
import com.ths.tradingai.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountFragment : Fragment() {

    private lateinit var balanceText: TextView
    private lateinit var marketValueText: TextView
    private lateinit var totalAssetsText: TextView
    private lateinit var posList: RecyclerView
    private lateinit var statusText: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        balanceText = view.findViewById(R.id.balanceText)
        marketValueText = view.findViewById(R.id.marketValueText)
        totalAssetsText = view.findViewById(R.id.totalAssetsText)
        posList = view.findViewById(R.id.posList)
        statusText = view.findViewById(R.id.statusText)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyText = view.findViewById(R.id.emptyText)

        posList.layoutManager = LinearLayoutManager(requireContext())

        swipeRefresh.setOnRefreshListener { loadData() }
        swipeRefresh.setColorSchemeColors(0xFF667eea.toInt())

        loadData()
    }

    private fun loadData() {
        val server = TokenManager.getServer(requireContext()) ?: return
        val token = TokenManager.getToken(requireContext()) ?: return

        swipeRefresh.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = ApiClient.getAccount(server, token)
                val status = ApiClient.getStatus(server, token)
                withContext(Dispatchers.Main) {
                    swipeRefresh.isRefreshing = false
                    if (account.success && account.data != null) {
                        val d = account.data
                        balanceText.text = formatMoney(d.balance)
                        marketValueText.text = formatMoney(d.marketValue)
                        totalAssetsText.text = formatMoney(d.totalAssets)

                        if (d.positions.isEmpty()) {
                            emptyText.visibility = View.VISIBLE
                            posList.visibility = View.GONE
                        } else {
                            emptyText.visibility = View.GONE
                            posList.visibility = View.VISIBLE
                            posList.adapter = PositionAdapter(d.positions)
                        }
                    }
                    if (status.success && status.data != null) {
                        val s = status.data
                        val connColor = if (s.connected) 0xFF3fb950.toInt() else 0xFFf85149.toInt()
                        val connText = if (s.connected) "已连接" else "未连接"
                        statusText.text = "系统: $connText | 自动交易: ${if (s.autoTrade) "开" else "关"} | 调度器: ${if (s.schedulerEnabled) "运行" else "停止"}"
                        statusText.setTextColor(connColor)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    swipeRefresh.isRefreshing = false
                    statusText.text = "网络错误: ${e.message}"
                    statusText.setTextColor(0xFFf85149.toInt())
                }
            }
        }
    }

    private fun formatMoney(v: Double): String {
        return if (v >= 10000) String.format("%.2f万", v / 10000) else String.format("%.2f", v)
    }
}

class PositionAdapter(private val positions: List<ApiClient.Position>) :
    RecyclerView.Adapter<PositionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.posName)
        val codeText: TextView = view.findViewById(R.id.posCode)
        val profitText: TextView = view.findViewById(R.id.posProfit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_position, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = positions[position]
        holder.nameText.text = p.name.ifEmpty { p.symbol }
        holder.codeText.text = "${p.symbol} · ${p.amount}股"
        val pnl = p.profit
        holder.profitText.text = if (pnl >= 0) "+${String.format("%.2f", pnl)}" else String.format("%.2f", pnl)
        holder.profitText.setTextColor(if (pnl >= 0) 0xFFf85149.toInt() else 0xFF3fb950.toInt())
    }

    override fun getItemCount() = positions.size
}
