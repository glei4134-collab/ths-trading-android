package com.ths.tradingai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ths.tradingai.util.TokenManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // 检查是否已绑定
        val token = TokenManager.getToken(this)
        if (token.isNullOrEmpty()) {
            navController.navigate(R.id.bindFragment)
            bottomNav.visibility = android.view.View.GONE
        }

        // 监听导航变化，控制底部栏显示
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.bindFragment -> bottomNav.visibility = android.view.View.GONE
                else -> bottomNav.visibility = android.view.View.VISIBLE
            }
        }
    }
}
