package hu.ugorjbe.app.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Explore
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.BuildConfig
import hu.ugorjbe.app.R
import hu.ugorjbe.app.ui.viewmodel.AuthMode
import hu.ugorjbe.app.ui.viewmodel.AuthUiState
import hu.ugorjbe.app.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val expanded = maxWidth >= 700.dp
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                BrandPanel(Modifier.weight(1f).fillMaxHeight())
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    AuthForm(state, viewModel, Modifier.padding(40.dp))
                }
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BrandPanel(Modifier.fillMaxWidth().height(220.dp))
                AuthForm(state, viewModel, Modifier.padding(top = 20.dp))
            }
        }
    }
}

@Composable
private fun BrandPanel(modifier: Modifier = Modifier) {
    Box(
        modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(bottomEnd = 32.dp)),
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).padding(28.dp).size(80.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
        )
        Box(
            Modifier.align(Alignment.BottomStart).padding(28.dp).size(18.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
        )
        Column(
            Modifier.align(Alignment.Center).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Outlined.Explore, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            Text(stringResource(R.string.auth_brand_story), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun AuthForm(state: AuthUiState, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var passwordVisible by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth().widthIn(max = 520.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    stringResource(if (state.mode == AuthMode.LOGIN) R.string.login_title else R.string.register_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (state.mode == AuthMode.REGISTER) {
                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = viewModel::setDisplayName,
                        label = { Text(stringResource(R.string.display_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::setEmail,
                    label = { Text(stringResource(R.string.email)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::setPassword,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password),
                            )
                        }
                    },
                )
                if (state.mode == AuthMode.REGISTER) {
                    Text(stringResource(R.string.password_hint), style = MaterialTheme.typography.bodyMedium)
                }
                state.error?.let {
                    Text(errorText(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !state.submitting,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    if (state.submitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        Text(stringResource(if (state.mode == AuthMode.LOGIN) R.string.login else R.string.register))
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Outlined.ArrowForward, null)
                    }
                }
                TextButton(onClick = viewModel::toggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(if (state.mode == AuthMode.LOGIN) R.string.switch_to_register else R.string.switch_to_login))
                }
            }
        }
        if (BuildConfig.DEBUG) {
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.demo_hint), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
