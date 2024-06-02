package com.example.fazendinha.ui.principal

import androidx.compose.foundation.layout.height
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fabricasapatos.ui.activities.principal.ui.theme.*

@ExperimentalMaterial3Api
@Composable
fun AppBar(
    onNavigationIconClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier
            .height(70.dp),
        title = {
            Text(text = "Fazendinha da Darla",
                    style = TextStyle(fontSize = 20.sp)
            )
        },
        backgroundColor = md_theme_light_tertiary,
        contentColor = md_theme_light_tertiary,
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle drawer"
                )
            }
        }
    )
}