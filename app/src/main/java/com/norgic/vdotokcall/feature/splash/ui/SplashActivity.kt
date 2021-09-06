package com.norgic.vdotokcall.feature.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.ActivitySplashBinding
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.feature.account.ui.AccountActivity.Companion.createAccountsActivity
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity.Companion.createDashBoardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        prefs = Prefs(this)

        moveToNextActivity()
    }

    private fun moveToNextActivity() {
        Handler(Looper.getMainLooper()).postDelayed({

            prefs.loginInfo?.let {
                startActivity(applicationContext?.let { createDashBoardActivity(it) })
                finish()

            } ?: kotlin.run {
                startActivity(createAccountsActivity(this))
                finish()

            }

        }, 2000)

    }

    companion object {

        const val API_ERROR = "API_ERROR"

    }
}
