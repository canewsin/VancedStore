package com.vanced.store.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vanced.store.ui.navigation.VSNavigation
import com.vanced.store.ui.navigation.VSNavigationScreen
import com.vanced.store.ui.navigation.rememberVSNavigatorBackstack
import com.vanced.store.ui.screen.*
import com.vanced.store.ui.theme.VSTheme
import com.vanced.store.ui.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            VSTheme(
                theme = viewModel.appTheme,
                accent = viewModel.appAccent
            ) {
                val systemUiController = rememberSystemUiController()
                val systemBarsColor = VSTheme.colorScheme.surface
                SideEffect {
                    systemUiController.setSystemBarsColor(systemBarsColor)
                }
                MainScreen()
            }
        }
    }

    @Composable
    private fun MainScreen() {
        val navigator = rememberVSNavigatorBackstack(initial = VSNavigationScreen.Browse)
        val currentScreen = navigator.current
        val bottomBarItems = VSNavigationScreen.bottomBarItems
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomBar(
                    currentScreen = currentScreen,
                    bottomBarItems = bottomBarItems,
                    onNavigate = { screen ->
                        navigator.navigate(screen)
                    }
                )
            }
        ) { paddingValues ->
            VSNavigation(
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
                transitionSpec = { navigationTransitionSpec(bottomBarItems) },
                backPressEnabled = true,
                onBackPress = {
                    if (navigator.current is VSNavigationScreen.Browse) {
                        finish()
                    } else {
                        navigator.back()
                    }
                }
            ) { screen ->
                when (screen) {
                    is VSNavigationScreen.Browse -> {
                        BrowseScreen(
                            navigator = navigator,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )
                    }
                    is VSNavigationScreen.Library -> {
                        LibraryScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        )
                    }
                    is VSNavigationScreen.More -> {
                        MoreScreen(
                            navigator = navigator,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                        )
                    }
                    is VSNavigationScreen.Search -> {
                        SearchScreen(
                            navigator = navigator,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    is VSNavigationScreen.Repositories -> {
                        RepositoriesScreen(navigator)
                    }
                    is VSNavigationScreen.Themes -> {
                        ThemesScreen(navigator)
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomBar(
        currentScreen: VSNavigationScreen,
        bottomBarItems: Array<VSNavigationScreen>,
        onNavigate: (VSNavigationScreen) -> Unit,
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = currentScreen in bottomBarItems,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut(),
        ) {
            NavigationBar(modifier = Modifier.fillMaxWidth()) {
                for (screen in VSNavigationScreen.bottomBarItems) {
                    VSScreenNavigationBarItem(
                        screen = screen,
                        selected = currentScreen == screen,
                        onClick = {
                            if (currentScreen != screen) {
                                onNavigate(screen)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun RowScope.VSScreenNavigationBarItem(
        screen: VSNavigationScreen,
        selected: Boolean,
        onClick: () -> Unit,
    ) {
        NavigationBarItem(
            selected = selected,
            icon = {
                Icon(
                    painter = painterResource(screen.iconRes),
                    contentDescription = null
                )
            },
            label = {
                Text(stringResource(screen.labelRes))
            },
            onClick = onClick
        )
    }

    private fun AnimatedContentScope<VSNavigationScreen>.navigationTransitionSpec(
        bottomBarItems: Array<VSNavigationScreen>
    ): ContentTransform {
        val initialIndex = bottomBarItems.indexOf(initialState)
        val targetIndex = bottomBarItems.indexOf(targetState)

        return if (initialIndex != -1 && targetIndex != -1) {
            if (initialIndex < targetIndex) {
                slideIntoContainer(
                    towards = AnimatedContentScope.SlideDirection.Start
                ) with slideOutOfContainer(
                    towards = AnimatedContentScope.SlideDirection.Start
                )
            } else {
                slideIntoContainer(
                    towards = AnimatedContentScope.SlideDirection.End
                ) with slideOutOfContainer(
                    towards = AnimatedContentScope.SlideDirection.End
                )
            }
        } else if (initialState == VSNavigationScreen.More) {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Start,
                initialOffset = { it / 3 }
            ) + fadeIn() with slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.Start,
                targetOffset = { it / 3 }
            ) + fadeOut()
        } else if (targetState == VSNavigationScreen.More) {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.End,
                initialOffset = { it / 3 }
            ) + fadeIn() with slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.End,
                targetOffset = { it / 3 }
            ) + fadeOut()
        } else {
            slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Up,
                initialOffset = { it / 5 }
            ) + fadeIn() with fadeOut()
        }
    }

}