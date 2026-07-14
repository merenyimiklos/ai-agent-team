package hu.ugorjbe.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.ugorjbe.app.R
import hu.ugorjbe.app.domain.OfferFilter
import hu.ugorjbe.app.ui.viewmodel.DiscoveryViewModel
import java.math.BigDecimal

private val categories = listOf<String?>(null, "PLAYHOUSE", "WORKSHOP", "MOVEMENT", "SWIMMING", "SPORT", "MUSEUM", "PARENT_CHILD")

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel, onOffer: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filtersVisible by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(stringResource(R.string.today_in_budapest), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.app_tagline), color = MaterialTheme.colorScheme.secondary)
            OutlinedTextField(
                value = state.filter.query,
                onValueChange = viewModel::setQuery,
                label = { Text(stringResource(R.string.search_hint)) },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { viewModel.refresh() }) { Icon(Icons.Outlined.Search, stringResource(R.string.discover)) }
                        IconButton(onClick = { filtersVisible = true }) { Icon(Icons.Outlined.FilterList, stringResource(R.string.filters)) }
                    }
                },
                singleLine = true,
            )
        }
        when {
            state.loading -> LoadingPane(Modifier.fillMaxSize())
            state.error != null -> ErrorPane(state.error!!, { viewModel.refresh() }, Modifier.fillMaxSize())
            state.offers.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.no_offers_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.no_offers_body), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { viewModel.applyFilter(OfferFilter()) }) { Text(stringResource(R.string.clear_filters)) }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.offers, key = { it.id }) { offer -> OfferCard(offer, { onOffer(offer.id) }) }
            }
        }
    }
    if (filtersVisible) {
        FilterDialog(
            current = state.filter,
            onDismiss = { filtersVisible = false },
            onApply = {
                filtersVisible = false
                viewModel.applyFilter(it)
            },
        )
    }
}

@Composable
private fun FilterDialog(current: OfferFilter, onDismiss: () -> Unit, onApply: (OfferFilter) -> Unit) {
    var draft by remember(current) { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filters)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text(stringResource(R.string.all_categories), fontWeight = FontWeight.SemiBold) }
                items(categories) { category ->
                    FilterChip(
                        selected = draft.category == category,
                        onClick = { draft = draft.copy(category = category) },
                        label = { Text(stringResource(category?.let(::categoryLabel) ?: R.string.all_categories)) },
                    )
                }
                item {
                    Text(stringResource(R.string.child_age), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 1).minus(1).coerceAtLeast(0)) }) { Text("−") }
                        Text(draft.childAge?.toString() ?: stringResource(R.string.any_age))
                        TextButton(onClick = { draft = draft.copy(childAge = (draft.childAge ?: 0).plus(1).coerceAtMost(18)) }) { Text("+") }
                        if (draft.childAge != null) TextButton(onClick = { draft = draft.copy(childAge = null) }) { Text(stringResource(R.string.clear_filters)) }
                    }
                }
                item {
                    Text(stringResource(R.string.start_window), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            null to R.string.start_today,
                            3 to R.string.start_next_three_hours,
                            6 to R.string.start_next_six_hours,
                        ).forEach { (hours, label) ->
                            FilterChip(
                                selected = draft.startsWithinHours == hours,
                                onClick = { draft = draft.copy(startsWithinHours = hours) },
                                label = { Text(stringResource(label)) },
                            )
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.maximum_price), fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf<BigDecimal?>(null, BigDecimal("5000"), BigDecimal("10000")).forEach { price ->
                            FilterChip(
                                selected = draft.maxPrice == price,
                                onClick = { draft = draft.copy(maxPrice = price) },
                                label = {
                                    Text(
                                        price?.let { stringResource(R.string.price_under_huf, it.toInt()) }
                                            ?: stringResource(R.string.any_price),
                                    )
                                },
                            )
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.minimum_places), fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces - 1).coerceAtLeast(1)) }) { Text("−") }
                        Text(draft.minAvailablePlaces.toString())
                        TextButton(onClick = { draft = draft.copy(minAvailablePlaces = (draft.minAvailablePlaces + 1).coerceAtMost(10)) }) { Text("+") }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.budapest_nearby), Modifier.weight(1f))
                        Switch(
                            checked = draft.nearBudapestCenter,
                            onCheckedChange = {
                                draft = draft.copy(
                                    nearBudapestCenter = it,
                                    sort = if (!it && draft.sort == "DISTANCE") "START_TIME" else draft.sort,
                                )
                            },
                        )
                    }
                }
                item {
                    Text(stringResource(R.string.sort), fontWeight = FontWeight.SemiBold)
                    listOf(
                        "START_TIME" to R.string.sort_time,
                        "PRICE" to R.string.sort_price,
                        "DISCOUNT" to R.string.sort_discount,
                    ).plus(if (draft.nearBudapestCenter) listOf("DISTANCE" to R.string.sort_distance) else emptyList()).forEach { (value, label) ->
                        FilterChip(
                            selected = draft.sort == value,
                            onClick = { draft = draft.copy(sort = value) },
                            label = { Text(stringResource(label)) },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onApply(draft) }) { Text(stringResource(R.string.apply_filters)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
