package hu.ugorjbe.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.R
import hu.ugorjbe.app.ui.theme.UgorjBeBrand
import hu.ugorjbe.app.ui.theme.UgorjBeMotion
import hu.ugorjbe.app.ui.theme.UgorjBeRadius
import hu.ugorjbe.app.ui.theme.UgorjBeSpacing
import hu.ugorjbe.app.ui.viewmodel.AuthMode
import hu.ugorjbe.app.ui.viewmodel.AuthUiState
import hu.ugorjbe.app.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val expanded = maxWidth >= 760.dp
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                BrandCanvas(Modifier.weight(0.9f).fillMaxHeight())
                Box(
                    Modifier.weight(1.1f).fillMaxHeight().statusBarsPadding().navigationBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    AuthForm(state, viewModel, Modifier.padding(48.dp))
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandCanvas(Modifier.fillMaxWidth().height(300.dp))
                AuthForm(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.padding(horizontal = UgorjBeSpacing.xl, vertical = UgorjBeSpacing.xxl),
                )
            }
        }
    }
}

@Composable
private fun BrandCanvas(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(UgorjBeBrand.HeroGradient)
            .statusBarsPadding()
            .padding(UgorjBeSpacing.xxl),
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(108.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(54.dp)
                .clip(CircleShape)
                .background(UgorjBeBrand.Apricot.copy(alpha = 0.82f)),
        )
        Column(
            modifier = Modifier.align(Alignment.CenterStart).widthIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
        ) {
            Surface(
                shape = RoundedCornerShape(UgorjBeRadius.pill),
                color = Color.White.copy(alpha = 0.14f),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Outlined.Explore, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.app_name), color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
            }
            Text(
                stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            Text(
                stringResource(R.string.auth_brand_story),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(UgorjBeSpacing.sm)) {
                BenefitBadge(Icons.Outlined.Bolt, stringResource(R.string.today))
                BenefitBadge(Icons.Outlined.LocationOn, "Közel")
                BenefitBadge(Icons.Outlined.Savings, "Kedvező")
            }
        }
    }
}

@Composable
private fun BenefitBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(shape = RoundedCornerShape(UgorjBeRadius.pill), color = Color.White.copy(alpha = 0.14f)) {
        Row(
            Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(icon, null, Modifier.size(16.dp), tint = Color.White)
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
    }
}

@Composable
private fun AuthForm(state: AuthUiState, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth().widthIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.xs)) {
            Text(
                stringResource(if (state.mode == AuthMode.LOGIN) R.string.login_title else R.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                stringResource(if (state.mode == AuthMode.LOGIN) R.string.switch_to_register else R.string.switch_to_login),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(UgorjBeRadius.hero),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            AnimatedContent(
                targetState = state.mode,
                transitionSpec = {
                    fadeIn(tween(UgorjBeMotion.Standard)) togetherWith fadeOut(tween(UgorjBeMotion.Quick)) using
                        SizeTransform(clip = false)
                },
                label = "auth-mode",
            ) { mode ->
                Column(
                    Modifier.padding(UgorjBeSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(UgorjBeSpacing.lg),
                ) {
                    if (mode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = state.displayName,
                            onValueChange = viewModel::setDisplayName,
                            label = { Text(stringResource(R.string.display_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(UgorjBeRadius.medium),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                        )
                    }
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::setEmail,
                        label = { Text(stringResource(R.string.email)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UgorjBeRadius.medium),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::setPassword,
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(UgorjBeRadius.medium),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password),
                                )
                            }
                        },
                    )
                    if (mode == AuthMode.REGISTER) {
                        Text(
                            stringResource(R.string.password_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    state.error?.let {
                        Surface(
                            shape = RoundedCornerShape(UgorjBeRadius.medium),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                errorText(it),
                                modifier = Modifier.padding(14.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Button(
                        onClick = viewModel::submit,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.submitting,
                        shape = RoundedCornerShape(UgorjBeRadius.medium),
                    ) {
                        if (state.submitting) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(stringResource(if (mode == AuthMode.LOGIN) R.string.login else R.string.register))
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Outlined.ArrowForward, null)
                        }
                    }
                    TextButton(onClick = viewModel::toggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(if (mode == AuthMode.LOGIN) R.string.switch_to_register else R.string.switch_to_login))
                    }
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Surface(
                shape = RoundedCornerShape(UgorjBeRadius.medium),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    stringResource(R.string.demo_hint),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
