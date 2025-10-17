package com.group.billpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.group.billpay.ui.AppNavigation
import com.group.billpay.ui.screens.BillSplitterScreen
// V V V TAMBAHKAN IMPORT INI V V V
import com.group.billpay.ui.theme.BillpayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hapus 'enableEdgeToEdge()' untuk sementara agar layout lebih sederhana
        setContent {
            BillpayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // V V V GANTI 'Greeting' DENGAN 'BillSplitterScreen' V V V
                    AppNavigation()
                }
            }
        }
    }
}