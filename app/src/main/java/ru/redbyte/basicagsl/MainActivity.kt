package ru.redbyte.basicagsl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.redbyte.basicagsl.example.gradient.AgslGradientDemoScreen
import ru.redbyte.basicagsl.example.ripple.AgslRippleDemoScreen
import ru.redbyte.basicagsl.example.star.AgslStarDemoScreen
import ru.redbyte.basicagsl.ui.theme.BasicAGSLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicAGSLTheme {
                InitNavigation()
            }
        }
    }

    @Composable
    private fun InitNavigation() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "mainScreen"
        ) {
            composable("mainScreen") { MainScreen(navController) }
            composable("agslGradientDemoScreen") { AgslGradientDemoScreen() }
            composable("agslRippleDemoScreen") { AgslRippleDemoScreen() }
            composable("agslStarDemoScreen") { AgslStarDemoScreen() }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Открыть экран:")
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate("agslGradientDemoScreen")
                }) {
                Text("Градиент")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("agslRippleDemoScreen") }) {
                Text("Волна от тапа")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("agslStarDemoScreen") }) {
                Text("Звезда")
            }

        }
    }
}


