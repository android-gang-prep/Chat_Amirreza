package com.ehsannarmani.apiprj_ehsan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ehsannarmani.apiprj_ehsan.navigation.Routes
import com.ehsannarmani.apiprj_ehsan.ui.screens.ActivationScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.AuthTypeScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.ChatScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.HomeScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.SignInScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.SignUpScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.SplashScreen
import com.ehsannarmani.apiprj_ehsan.ui.screens.StreamScreen
import com.ehsannarmani.apiprj_ehsan.ui.theme.ApiPrjEhsanTheme

class MainActivity : ComponentActivity() {
    private var isOnline by mutableStateOf(false)

    override fun onPause() {
        isOnline = false
        super.onPause()
    }

    override fun onResume() {
        isOnline = true
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ApiPrjEhsanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Routes.Splash.route){
                        composable(Routes.Splash.route){
                            SplashScreen(navController = navController)
                        }
                        composable(Routes.AuthType.route){
                            AuthTypeScreen(navController = navController)
                        }
                        composable(Routes.SignUp.route){
                            SignUpScreen(navController = navController)
                        }
                        composable(Routes.Activation.route){
                            ActivationScreen(navController = navController)
                        }
                        composable(Routes.SignIn.route){
                            SignInScreen(navController = navController)
                        }
                        composable(Routes.Home.route){
                            HomeScreen(navController = navController, isOnline)
                        }
                        composable(Routes.Chat.route + "?to={to}", arguments = listOf(
                            navArgument("to"){
                                type = NavType.StringType
                            }
                        )){
                            val to = it.arguments?.getString("to")!!
                            ChatScreen(to = to)
                        }
                        composable(
                            Routes.Stream.route,
                            enterTransition = {
                                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(500))
                            },
                            popEnterTransition = {
                                slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(500))
                            },
                            exitTransition = {
                                slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(500))
                            }
                        ){
                            StreamScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

