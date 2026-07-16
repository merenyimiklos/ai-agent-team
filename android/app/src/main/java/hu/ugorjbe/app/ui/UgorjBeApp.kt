package hu.ugorjbe.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
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
import hu.ugorjbe.app.ui.theme.UgorjBeMotion
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
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
    AnimatedContent(
        targetState = session,
        transitionSpec = {
            fadeIn(tween(UgorjBeMotion.Standard)) togetherWith fadeOut(tween(UgorjBeMotion.Quick))
        },
        contentKey = { it?.user?.id ?: "auth" },
        label = "session-gate",
    ) { currentSession ->
        if (currentSession == null) {
            AuthScreen(viewModel = hiltViewModel<AuthViewModel>())
        } else {
            MainShell(
                displayName = currentSession.user.displayName,
                email = currentSession.user.email,
                onLogout = sessionViewModel::logout,
            )
        }
    }
}

@Composable
private fun MainShell(displayName: String, email: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val topLevel = CustomerNavigation.isTopLevel(route)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
    ) {
        val compact = maxWidth < 600.dp
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (compact && topLevel) {
                    Phase4BottomDock(route = route, navController = navController)
                }
            },
        ) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (!compact && topLevel) {
                    Phase4NavigationRail(route = route, navController = navController)
                }
                Box(Modifier.weight(1f).fillMaxSize()) {
                    CustomerNavHost(navController, displayName, email, onLogout)
                }
            }
        }
    }
}

@Composable
private fun Phase4BottomDock(route: String?, navController: NavHostController) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(UgorjBeRadius.hero),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 10.dp,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                destinations.forEach { destination ->
                    Phase4DockItem(
                        destination = destination,
                        selected = route == destination.route,
                        onClick = { navController.openTopLevel(destination.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Phase4DockItem(
    destination: TopDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = UgorjBeMotion.tactileSpring(),
        label = "dock-press",
    )
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "dock-background",
    )
    val foreground by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "dock-foreground",
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .testTag("nav_${destination.route}")
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                ) {}
            }
            Icon(
                imageVector = if (selected) destination.selectedIcon else destination.icon,
                contentDescription = stringResource(destination.label),
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.onSecondary else foreground,
            )
        }
        Text(
            text = stringResource(destination.label),
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun Phase4NavigationRail(route: String?, navController: NavHostController) {
    Surface(
        modifier = Modifier.width(96.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.secondary,
        shadowElevation = 3.dp,
    ) {
        NavigationRail(
            containerColor = Color.Transparent,
            header = {
                UgorjBeBrandMark(
                    modifier = Modifier.padding(vertical = UgorjBeSpacing.xxl).size(48.dp),
                    inverse = true,
                )
            },
        ) {
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
                    modifier = Modifier.testTag("nav_${destination.route}"),
                )
                Spacer(Modifier.size(4.dp))
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
        composable(
            route = "discover",
            enterTransition = { fadeIn(tween(UgorjBeMotion.Standard)) },
            exitTransition = { fadeOut(tween(UgorjBeMotion.Quick)) },
        ) {
            Phase4DiscoveryScreen(
                viewModel = hiltViewModel<DiscoveryViewModel>(),
                onOffer = { navController.navigate("offer/$it") },
            )
        }
        composable(
            route = "bookings",
            enterTransition = { fadeIn(tween(UgorjBeMotion.Standard)) },
            exitTransition = { fadeOut(tween(UgorjBeMotion.Quick)) },
        ) {
            BookingsScreen(hiltViewModel<BookingsViewModel>())
        }
        composable(
            route = "favorites",
            enterTransition = { fadeIn(tween(UgorjBeMotion.Standard)) },
            exitTransition = { fadeOut(tween(UgorjBeMotion.Quick)) },
        ) {
            FavoritesScreen(
                viewModel = hiltViewModel<FavoritesViewModel>(),
                onOffer = { navController.navigate("offer/$it") },
                onProvider = { navController.navigate("provider/$it") },
            )
        }
        composable(
            route = "profile",
            enterTransition = { fadeIn(tween(UgorjBeMotion.Standard)) },
            exitTransition = { fadeOut(tween(UgorjBeMotion.Quick)) },
        ) {
            ProfileScreen(displayName = displayName, email = email, onLogout = onLogout)
        }
        composable(
            route = "offer/{offerId}",
            enterTransition = {
                fadeIn(tween(UgorjBeMotion.Standard)) +
                    slideInHorizontally(tween(UgorjBeMotion.Standard)) { it / 7 }
            },
            exitTransition = {
                fadeOut(tween(UgorjBeMotion.Quick)) +
                    slideOutHorizontally(tween(UgorjBeMotion.Quick)) { it / 7 }
            },
        ) {
            OfferDetailScreen(
                viewModel = hiltViewModel<OfferDetailViewModel>(),
                onBack = navController::popBackStack,
                onProvider = { navController.navigate("provider/$it") },
                onBookings = { navController.openTopLevel("bookings") },
            )
        }
        composable(
            route = "provider/{providerId}",
            enterTransition = {
                fadeIn(tween(UgorjBeMotion.Standard)) +
                    slideInHorizontally(tween(UgorjBeMotion.Standard)) { it / 7 }
            },
            exitTransition = {
                fadeOut(tween(UgorjBeMotion.Quick)) +
                    slideOutHorizontally(tween(UgorjBeMotion.Quick)) { it / 7 }
            },
        ) {
            ProviderScreen(
                viewModel = hiltViewModel<ProviderViewModel>(),
                onBack = navController::popBackStack,
            )
        }
    }
}
