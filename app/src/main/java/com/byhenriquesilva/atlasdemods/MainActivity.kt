package com.byhenriquesilva.atlasdemods

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.byhenriquesilva.atlasdemods.ui.nav.AtlasNavHost
import com.byhenriquesilva.atlasdemods.ui.theme.AtlasDeModsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AtlasApp
        setContent {
            AtlasDeModsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AtlasNavHost(repository = app.repository, secretStore = app.secretStore)
                }
            }
        }
    }
}
