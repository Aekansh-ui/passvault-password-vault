package com.example.password_vault

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.password_vault.security.SessionManager
import com.example.password_vault.ui.navigation.PassVaultNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PassVaultNavGraph(sessionManager = sessionManager)
        }
    }

    override fun onResume() {
        super.onResume()
        sessionManager.checkTimeout()
    }
}
