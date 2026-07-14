package hu.ugorjbe.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hu.ugorjbe.app.R
import hu.ugorjbe.app.ui.viewmodel.AuthViewModel
import hu.ugorjbe.app.ui.viewmodel.BookingsViewModel
import hu.ugorjbe.app.ui.viewmodel.DiscoveryViewModel
import hu.ugorjbe.app.ui.viewmodel.FavoritesViewModel
import hu.ugorjbe.app.ui.viewmodel.OfferDetailViewModel
import hu.ugorjbe.app.ui.viewmodel.ProviderViewModel
import hu.ugorjbe.app.ui.viewmodel.SessionViewModel

private data class TopDestination(
    val route: String,
    val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
)

internal object CustomerNavigation {
    val topLevelRoutes = listOf("discover", "bookings", "favorites", "profile")
    fun isTopLevel(route: String?) = route in topLevelRoutes
}

private val destinations = listOf(
    TopDestination("discover", R.string.discover, Icons.Outlined.Explore, Icons.Filled.Explore),
    TopDestination("bookings", R.string.bookings, Icons.Outlined.ConfirmationNumber, Icons.Filled.ConfirmationNumber),
    TopDestination("favorites", R.string.favorites, Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    TopDestination("profile", R.string.profile, Icons.Outlined.Person, Icons.Filled.Person),
)

@Composable
fun UgorjBeApp(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    if (session == null) {
        AuthScreen(viewModel = hiltViewModel<AuthViewModel>())
    } else {
        MainShell(
            displayName = session!!.user.displayName,
            email = session!!.user.email,
            onLogout = sessionViewModel::logout,
        )
    }
}

@Composable
private fun MainShell(displayName: String, email: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val topLevel = CustomerNavigation.isTopLevel(route)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 600.dp
        Scaffold(
            bottomBar = {
                if (compact && topLevel) {
                    NavigationBar(tonalElevation = 3.dp) {
                        destinations.forEach { destination ->
                            val selected = route == destination.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navController.openTopLevel(destination.route) },
                                icon = {
                                    Icon(
                                        if (selected) destination.selectedIcon else destination.icon,
                                        stringResource(destination.label),
                                    )
                                },
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (!compact && topLevel) {
                    NavigationRail {
                        destinations.forEach { destination ->
                            val selected = route == destination.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navController.openTopLevel(destination.route) },
                                icon = {
                                    Icon(
                                        if (selected) destination.selectedIcon else destination.icon,
                                        stringResource(destination.label),
                                    )
                                },
                                label = { Text(stringResource(destination.label)) },
                            )
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxSize()) {
                    CustomerNavHost(navController, displayName, email, onLogout)
                }
            }
        }
    }
}

private fun NavController.openTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun CustomerNavHost(
    navController: NavHostController,
    displayName: String,
    email: String,
    onLogout: () -> Unit,
) {
    NavHost(navController = navController, startDestination = "discover") {
        composable("discover") {
            DiscoveryScreen(
                viewModel = hiltViewModel<DiscoveryViewModel>(),
                onOffer = { navController.navigate("offer/$it") },
            )
        }
        composable("bookings") { BookingsScreen(hiltViewModel<BookingsViewModel>()) }
        composable("favorites") {
            FavoritesScreen(
                viewModel = hiltViewModel<FavoritesViewModel>(),
                onOffer = { navController.navigate("offer/$it") },
                onProvider = { navController.navigate("provider/$it") },
            )
        }
        composable("profile") {
            ProfileScreen(displayName = displayName, email = email, onLogout = onLogout)
        }
        composable("offer/{offerId}") {
            OfferDetailScreen(
                viewModel = hiltViewModel<OfferDetailViewModel>(),
                onBack = navController::popBackStack,
                onProvider = { navController.navigate("provider/$it") },
                onBookings = { navController.openTopLevel("bookings") },
            )
        }
        composable("provider/{providerId}") {
            ProviderScreen(
                viewModel = hiltViewModel<ProviderViewModel>(),
                onBack = navController::popBackStack,
            )
        }
    }
}
