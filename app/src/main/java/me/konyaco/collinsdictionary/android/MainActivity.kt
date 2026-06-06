package me.konyaco.collinsdictionary.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.konyaco.collinsdictionary.android.ui.CollinsDictionaryApp
import me.konyaco.collinsdictionary.android.ui.CollinsDictionaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CollinsDictionaryTheme {
                CollinsDictionaryApp()
            }
        }
    }
}
