@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.journexui.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.journexui.model.*
import com.example.journexui.network.RetrofitClient
import com.example.journexui.network.extractErrorMessage
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private fun auth(token: String) = "Bearer $token"
private fun nowIso() = LocalDateTime.now().toString()

enum class Page { DASHBOARD, TRADES, STRATEGIES, CHECKLISTS, PROFILE, TRASH }

@Composable
fun AppScreen(token: String, onLogout: () -> Unit) {
    var page by remember { mutableStateOf(Page.DASHBOARD) }
    var message by remember { mutableStateOf("Ready") }
    val notify: (String) -> Unit = { message = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journex") },
                actions = {
                    IconButton(onClick = { page = Page.PROFILE }) { Icon(Icons.Default.Person, "Profile") }
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout") }
                }
            )
        },
        bottomBar = { JournexStatusBar(message) }
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            NavigationRail(modifier = Modifier.fillMaxHeight()) {
                RailItem(Icons.Default.Dashboard, "Dashboard", page == Page.DASHBOARD) { page = Page.DASHBOARD }
                RailItem(Icons.Default.ShowChart, "Trades", page == Page.TRADES) { page = Page.TRADES }
                RailItem(Icons.Default.AutoGraph, "Strategies", page == Page.STRATEGIES) { page = Page.STRATEGIES }
                RailItem(Icons.Default.Checklist, "Checklists", page == Page.CHECKLISTS) { page = Page.CHECKLISTS }
                RailItem(Icons.Default.Delete, "Trash", page == Page.TRASH) { page = Page.TRASH }
            }
            Box(Modifier.fillMaxSize().padding(24.dp)) {
                when (page) {
                    Page.DASHBOARD -> Dashboard(token, { page = Page.TRADES }, notify)
                    Page.TRADES -> TradesPage(token, notify)
                    Page.STRATEGIES -> StrategiesPage(token, notify)
                    Page.CHECKLISTS -> ChecklistsPage(token, notify)
                    Page.PROFILE -> ProfilePage(token, notify)
                    Page.TRASH -> TrashPage(token, notify)
                }
            }
        }
    }
}

@Composable
private fun RailItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, null) },
        label = { Text(label) }
    )
}

@Composable
private fun Dashboard(token: String, openTrades: () -> Unit, notify: (String) -> Unit) {
    var user by remember { mutableStateOf<UserDto?>(null) }
    var trades by remember { mutableStateOf<List<TradeDto>>(emptyList()) }
    var strategies by remember { mutableStateOf<List<StrategyDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                user = RetrofitClient.api.me(auth(token))
                trades = RetrofitClient.api.trades(auth(token), 0, 8).content
                strategies = RetrofitClient.api.strategies(auth(token), 0, 8).content
            } catch (e: Exception) {
                notify(extractErrorMessage(e))
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Good to see you, ${user?.nickname?.ifBlank { null } ?: user?.username ?: "trader"}", style = MaterialTheme.typography.headlineLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard("Open trades", trades.count { it.status == "OPEN" }.toString(), Modifier.weight(1f))
            MetricCard("Loaded trades", trades.size.toString(), Modifier.weight(1f))
            MetricCard("Strategies", strategies.size.toString(), Modifier.weight(1f))
            MetricCard("Plan", user?.subscriptionPlan ?: "FREE", Modifier.weight(1f))
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Recent trades", style = MaterialTheme.typography.titleLarge)
                if (trades.isEmpty()) Text("No trades found") else trades.take(5).forEach { TradeRow(it) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = openTrades) { Text("View trades") }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun TradeRow(trade: TradeDto) {
    ListItem(
        headlineContent = { Text("${trade.symbol} • ${trade.tradePositionSide ?: ""}") },
        supportingContent = { Text("${trade.status ?: ""} | entry ${trade.entryPrice} | P/L ${trade.profitLoss ?: 0.0}") },
        leadingContent = { Icon(if (trade.status == "CLOSED") Icons.Default.CheckCircle else Icons.Default.Schedule, null) }
    )
}

@Composable
private fun StrategiesPage(token: String, notify: (String) -> Unit) {
    var data by remember { mutableStateOf<ApiPage<StrategyDto>?>(null) }
    var selected by remember { mutableStateOf<StrategyDto?>(null) }
    var showForm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            try { data = RetrofitClient.api.strategies(auth(token), 0, 50) }
            catch (e: Exception) { notify(extractErrorMessage(e)) }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Strategies", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { selected = null; showForm = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("New strategy") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data?.content ?: emptyList()) { strategy ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(strategy.name) },
                        supportingContent = { Text("${strategy.tradeType ?: "OTHER"} • ${strategy.tradeMarketType ?: ""} • ${strategy.tradeTimeframe ?: ""}\nRisk ${strategy.risk ?: 0} / Reward ${strategy.reward ?: 0} • ${strategy.riskPercent ?: 0}%") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { selected = strategy; showForm = true }) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = {
                                    scope.launch {
                                        try {
                                            strategy.id?.let { RetrofitClient.api.deleteStrategy(auth(token), it) }
                                            notify("Strategy deleted")
                                            load()
                                        } catch (e: Exception) { notify(extractErrorMessage(e)) }
                                    }
                                }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showForm) {
        StrategyDialog(token, selected, { showForm = false; load(); notify("Strategy saved") }, { showForm = false }, notify)
    }
}

@Composable
private fun StrategyDialog(token: String, item: StrategyDto?, onSaved: () -> Unit, onCancel: () -> Unit, notify: (String) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var type by remember { mutableStateOf(item?.tradeType ?: tradeTypes.first()) }
    var market by remember { mutableStateOf(item?.tradeMarketType ?: marketTypes.first()) }
    var timeframe by remember { mutableStateOf(item?.tradeTimeframe ?: "H1") }
    var risk by remember { mutableStateOf(item?.risk?.toString() ?: "") }
    var reward by remember { mutableStateOf(item?.reward?.toString() ?: "") }
    var riskPercent by remember { mutableStateOf(item?.riskPercent?.toString() ?: "") }
    var publicStrategy by remember { mutableStateOf(item?.publicStrategy ?: false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (item == null) "New strategy" else "Edit strategy") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                TextField(description, { description = it }, label = { Text("Description") })
                EnumMenu("Type", type, tradeTypes) { type = it }
                EnumMenu("Market", market, marketTypes) { market = it }
                EnumMenu("Timeframe", timeframe, timeframes) { timeframe = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(risk, { risk = it }, label = { Text("Risk") }, modifier = Modifier.weight(1f))
                    TextField(reward, { reward = it }, label = { Text("Reward") }, modifier = Modifier.weight(1f))
                    TextField(riskPercent, { riskPercent = it }, label = { Text("Risk %") }, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(publicStrategy, { publicStrategy = it })
                    Text("Public strategy")
                }
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = {
                scope.launch {
                    try {
                        val dto = StrategyDto(
                            id = item?.id, address = item?.address ?: "", name = name, description = description,
                            userId = item?.userId, checklistIds = item?.checklistIds ?: emptyList(), tradeType = type,
                            tradeMarketType = market, tradeTimeframe = timeframe, risk = risk.toLongOrNull(),
                            reward = reward.toLongOrNull(), riskPercent = riskPercent.toIntOrNull(), publicStrategy = publicStrategy
                        )
                        if (item?.id == null) RetrofitClient.api.saveStrategy(auth(token), dto)
                        else RetrofitClient.api.updateStrategy(auth(token), item.id, dto)
                        onSaved()
                    } catch (e: Exception) { notify(extractErrorMessage(e)) }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun ChecklistsPage(token: String, notify: (String) -> Unit) {
    var data by remember { mutableStateOf<ApiPage<ChecklistDto>?>(null) }
    var selected by remember { mutableStateOf<ChecklistDto?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<ChecklistDto?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            try { data = RetrofitClient.api.checklists(auth(token), 0, 50) }
            catch (e: Exception) { notify(extractErrorMessage(e)) }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Checklists", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { selected = null; showForm = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("New checklist") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data?.content ?: emptyList()) { checklist ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(checklist.name) },
                        supportingContent = { Text("${checklist.scope} • ${checklist.checklistCategory}\n${checklist.description ?: "No description"}") },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { detail = checklist }) { Icon(Icons.Default.List, "Items") }
                                IconButton(onClick = { selected = checklist; showForm = true }) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = {
                                    scope.launch {
                                        try {
                                            checklist.id?.let { RetrofitClient.api.deleteChecklist(auth(token), it) }
                                            notify("Checklist deleted")
                                            load()
                                        } catch (e: Exception) { notify(extractErrorMessage(e)) }
                                    }
                                }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    )
                }
            }
        }
    }
    if (showForm) ChecklistDialog(token, selected, { showForm = false; load(); notify("Checklist saved") }, { showForm = false }, notify)
    detail?.let { ChecklistDetailDialog(token, it, { detail = null }, notify) }
}

@Composable
private fun ChecklistDialog(token: String, item: ChecklistDto?, onSaved: () -> Unit, onCancel: () -> Unit, notify: (String) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var scopeValue by remember { mutableStateOf(item?.scope ?: "PRE_TRADE") }
    var category by remember { mutableStateOf(item?.checklistCategory ?: "ENTRY_SETUP") }
    var publicValue by remember { mutableStateOf(item?.publicChecklist ?: false) }
    var active by remember { mutableStateOf(item?.active ?: true) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (item == null) "New checklist" else "Edit checklist") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(name, { name = it }, label = { Text("Name") })
                TextField(description, { description = it }, label = { Text("Description") })
                EnumMenu("Scope", scopeValue, checklistScopes) { scopeValue = it }
                EnumMenu("Category", category, checklistCategories) { category = it }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(publicValue, { publicValue = it }); Text("Public checklist") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(active, { active = it }); Text("Active") }
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = {
                coroutineScope.launch {
                    try {
                        val dto = ChecklistDto(
                            id = item?.id, name = name, description = description, scope = scopeValue,
                            checklistCategory = category, strategyIds = item?.strategyIds ?: emptyList(),
                            userId = item?.userId, itemIds = item?.itemIds ?: emptyList(), publicChecklist = publicValue,
                            active = active
                        )
                        if (item?.id == null) RetrofitClient.api.saveChecklist(auth(token), dto)
                        else RetrofitClient.api.updateChecklist(auth(token), item.id, dto)
                        onSaved()
                    } catch (e: Exception) { notify(extractErrorMessage(e)) }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun ChecklistDetailDialog(token: String, checklist: ChecklistDto, onDismiss: () -> Unit, notify: (String) -> Unit) {
    var itemsData by remember { mutableStateOf<List<ChecklistItemDto>>(emptyList()) }
    var showAdd by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun load() {
        coroutineScope.launch {
            try { checklist.id?.let { itemsData = RetrofitClient.api.checklistItems(auth(token), it, 0, 100).content } }
            catch (e: Exception) { notify(extractErrorMessage(e)) }
        }
    }
    LaunchedEffect(checklist.id) { load() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checklist items: ${checklist.name}") },
        text = {
            Column(Modifier.heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(itemsData) { item ->
                        ListItem(
                            headlineContent = { Text(item.value.ifBlank { "Untitled item" }) },
                            supportingContent = { Text("${item.type} • order ${item.orderIndex} • ${if (item.required) "required" else "optional"}") },
                            trailingContent = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try { item.id?.let { RetrofitClient.api.deleteItem(auth(token), it) }; notify("Item deleted"); load() }
                                        catch (e: Exception) { notify(extractErrorMessage(e)) }
                                    }
                                }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        )
                    }
                }
                Button(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add question") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
    if (showAdd) ChecklistItemDialog(token, checklist.id ?: 0L, null, { showAdd = false; load(); notify("Item added") }, { showAdd = false }, notify)
}

@Composable
private fun ChecklistItemDialog(token: String, checklistId: Long, item: ChecklistItemDto?, onSaved: () -> Unit, onCancel: () -> Unit, notify: (String) -> Unit) {
    var value by remember { mutableStateOf(item?.value ?: "") }
    var type by remember { mutableStateOf(item?.type ?: "QUESTION_BOOLEAN") }
    var required by remember { mutableStateOf(item?.required ?: false) }
    var order by remember { mutableStateOf(item?.orderIndex?.toString() ?: "0") }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (item == null) "Add question" else "Edit item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value, { value = it }, label = { Text("Question/value") })
                EnumMenu("Type", type, checklistItemTypes) { type = it }
                TextField(order, { order = it }, label = { Text("Order") })
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(required, { required = it }); Text("Required") }
            }
        },
        confirmButton = {
            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val dto = ChecklistItemDto(item?.id, value, type, required, order.toLongOrNull() ?: 0L, checklistId)
                        if (item?.id == null) RetrofitClient.api.addQuestion(auth(token), dto)
                        else RetrofitClient.api.updateItem(auth(token), item.id, dto)
                        onSaved()
                    } catch (e: Exception) { notify(extractErrorMessage(e)) }
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun TradesPage(token: String, notify: (String) -> Unit) {
    var data by remember { mutableStateOf<ApiPage<TradeDto>?>(null) }
    var status by remember { mutableStateOf("ALL") }
    var selected by remember { mutableStateOf<TradeDto?>(null) }
    var showOpen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun load() {
        coroutineScope.launch {
            try {
                data = if (status == "ALL") RetrofitClient.api.trades(auth(token), 0, 100)
                else RetrofitClient.api.tradesByStatus(auth(token), status, 0, 100)
            } catch (e: Exception) { notify(extractErrorMessage(e)) }
        }
    }
    LaunchedEffect(status) { load() }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Trades", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = { showOpen = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Open trade") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { EnumMenu("Status", status, listOf("ALL") + tradeStatuses) { status = it } }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data?.content ?: emptyList()) { trade ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("${trade.symbol} • ${trade.tradePositionSide ?: ""}") },
                        supportingContent = { Text("${trade.status ?: ""} • ${trade.tradeMarketType ?: ""} • ${trade.tradeTimeframe ?: ""}\nEntry ${trade.entryPrice} • SL ${trade.stopLoss ?: "-"} • TP ${trade.takeProfit ?: "-"} • P/L ${trade.profitLoss ?: 0.0}") },
                        trailingContent = {
                            Row {
                                if (trade.status == "OPEN") IconButton(onClick = { selected = trade }) { Icon(Icons.Default.Check, "Close") }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try { trade.id?.let { RetrofitClient.api.deleteTrade(auth(token), it) }; notify("Trade deleted"); load() }
                                        catch (e: Exception) { notify(extractErrorMessage(e)) }
                                    }
                                }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    )
                }
            }
        }
    }
    selected?.let { CloseTradeDialog(token, it, { selected = null; load(); notify("Trade closed") }, { selected = null }, notify) }
    if (showOpen) OpenTradeDialog(token, { showOpen = false; load(); notify("Trade opened") }, { showOpen = false }, notify)
}

@Composable
private fun OpenTradeDialog(token: String, onSaved: () -> Unit, onCancel: () -> Unit, notify: (String) -> Unit) {
    var symbol by remember { mutableStateOf("") }
    var side by remember { mutableStateOf("BUY") }
    var market by remember { mutableStateOf("FOREX") }
    var type by remember { mutableStateOf("DAY_TRADING") }
    var timeframe by remember { mutableStateOf("H1") }
    var entry by remember { mutableStateOf("") }
    var lot by remember { mutableStateOf("1") }
    var leverage by remember { mutableStateOf("") }
    var stopLoss by remember { mutableStateOf("") }
    var takeProfit by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Open trade") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                TextField(symbol, { symbol = it }, label = { Text("Symbol") })
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    TextField(entry, { entry = it }, label = { Text("Entry price") }, modifier = Modifier.weight(1f))
                    TextField(lot, { lot = it }, label = { Text("Lot size") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { EnumMenu("Side", side, positionSides) { side = it }; EnumMenu("Market", market, marketTypes) { market = it } }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { EnumMenu("Type", type, tradeTypes) { type = it }; EnumMenu("Timeframe", timeframe, timeframes) { timeframe = it } }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    TextField(stopLoss, { stopLoss = it }, label = { Text("Stop loss") }, modifier = Modifier.weight(1f))
                    TextField(takeProfit, { takeProfit = it }, label = { Text("Take profit") }, modifier = Modifier.weight(1f))
                }
                TextField(leverage, { leverage = it }, label = { Text("Leverage") })
                TextField(balance, { balance = it }, label = { Text("Balance before") })
                TextField(emotion, { emotion = it }, label = { Text("Emotion before") })
                TextField(description, { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(enabled = symbol.isNotBlank() && entry.toDoubleOrNull() != null, onClick = {
                coroutineScope.launch {
                    try {
                        RetrofitClient.api.openTrade(
                            auth(token),
                            TradeOpenRequestDto(
                                description = description,
                                tradeMarketType = market,
                                tradeTimeframe = timeframe,
                                tradeType = type,
                                tradePositionSide = side,
                                symbol = symbol,
                                lotSize = lot.toDoubleOrNull() ?: 0.0,
                                leverage = leverage.toIntOrNull(),
                                entryPrice = entry.toDouble(),
                                entryTime = nowIso(),
                                stopLoss = stopLoss.toDoubleOrNull(),
                                takeProfit = takeProfit.toDoubleOrNull(),
                                balanceBeforeTrade = balance.toDoubleOrNull(),
                                emotionBefore = emotion
                            )
                        )
                        onSaved()
                    } catch (e: Exception) { notify(extractErrorMessage(e)) }
                }
            }) { Text("Open") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun CloseTradeDialog(token: String, trade: TradeDto, onSaved: () -> Unit, onCancel: () -> Unit, notify: (String) -> Unit) {
    var exitPrice by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("") }
    var swap by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Close ${trade.symbol}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Entry ${trade.entryPrice}")
                TextField(exitPrice, { exitPrice = it }, label = { Text("Exit price") })
                TextField(commission, { commission = it }, label = { Text("Commission") })
                TextField(swap, { swap = it }, label = { Text("Swap") })
                TextField(balance, { balance = it }, label = { Text("Balance after") })
                TextField(emotion, { emotion = it }, label = { Text("Emotion after") })
            }
        },
        confirmButton = {
            Button(enabled = exitPrice.toDoubleOrNull() != null, onClick = {
                coroutineScope.launch {
                    try {
                        RetrofitClient.api.closeTrade(
                            auth(token), trade.id ?: return@launch,
                            TradeCloseRequestDto(exitPrice.toDouble(), nowIso(), commission.toDoubleOrNull(), swap.toDoubleOrNull(), balance.toDoubleOrNull(), emotion)
                        )
                        onSaved()
                    } catch (e: Exception) { notify(extractErrorMessage(e)) }
                }
            }) { Text("Close trade") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

@Composable
private fun ProfilePage(token: String, notify: (String) -> Unit) {
    var user by remember { mutableStateOf<UserDto?>(null) }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun load() {
        coroutineScope.launch {
            try {
                user = RetrofitClient.api.me(auth(token))
                nickname = user?.nickname ?: ""
                phone = user?.phoneNumber ?: ""
            } catch (e: Exception) { notify(extractErrorMessage(e)) }
            finally { loading = false }
        }
    }
    LaunchedEffect(Unit) { load() }

    if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineLarge)
        ElevatedCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Account", style = MaterialTheme.typography.titleLarge)
                Text("Username: ${user?.username}")
                Text("Email: ${user?.email}")
                Text("Plan: ${user?.subscriptionPlan ?: "FREE"}")
                Text("Role: ${user?.role ?: "USER"}")
            }
        }
        ElevatedCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Edit profile", style = MaterialTheme.typography.titleLarge)
                TextField(nickname, { nickname = it }, label = { Text("Nickname") })
                TextField(phone, { phone = it }, label = { Text("Phone") })
                Button(onClick = {
                    coroutineScope.launch {
                        try { user = RetrofitClient.api.updateProfile(auth(token), UpdateProfileDto(nickname, null, phone)); notify("Profile updated") }
                        catch (e: Exception) { notify(extractErrorMessage(e)) }
                    }
                }) { Text("Save profile") }
            }
        }
        ElevatedCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Change password", style = MaterialTheme.typography.titleLarge)
                TextField(oldPassword, { oldPassword = it }, label = { Text("Old password") })
                TextField(newPassword, { newPassword = it }, label = { Text("New password") })
                Button(enabled = oldPassword.isNotBlank() && newPassword.isNotBlank(), onClick = {
                    coroutineScope.launch {
                        try { RetrofitClient.api.changePassword(auth(token), ChangePasswordDto(oldPassword, newPassword)); oldPassword = ""; newPassword = ""; notify("Password changed") }
                        catch (e: Exception) { notify(extractErrorMessage(e)) }
                    }
                }) { Text("Change password") }
            }
        }
    }
}

@Composable
private fun TrashPage(token: String, notify: (String) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    var strategies by remember { mutableStateOf<List<StrategyDto>>(emptyList()) }
    var checklists by remember { mutableStateOf<List<ChecklistDto>>(emptyList()) }
    var trades by remember { mutableStateOf<List<TradeDto>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    fun load() {
        coroutineScope.launch {
            try {
                strategies = RetrofitClient.api.deletedStrategies(auth(token), 0, 100).content
                checklists = RetrofitClient.api.deletedChecklists(auth(token), 0, 100).content
                trades = RetrofitClient.api.deletedTrades(auth(token), 0, 100).content
            } catch (e: Exception) { notify(extractErrorMessage(e)) }
        }
    }
    LaunchedEffect(Unit) { load() }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Trash", style = MaterialTheme.typography.headlineLarge)
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text("Strategies") })
            Tab(tab == 1, { tab = 1 }, text = { Text("Checklists") })
            Tab(tab == 2, { tab = 2 }, text = { Text("Trades") })
        }
        LazyColumn {
            when (tab) {
                0 -> items(strategies) { strategy ->
                    ListItem(headlineContent = { Text(strategy.name) }, trailingContent = {
                        IconButton(onClick = {
                            coroutineScope.launch { try { strategy.id?.let { RetrofitClient.api.restoreStrategy(auth(token), it) }; notify("Strategy restored"); load() } catch (e: Exception) { notify(extractErrorMessage(e)) } }
                        }) { Icon(Icons.Default.Restore, "Restore") }
                    })
                }
                1 -> items(checklists) { checklist ->
                    ListItem(headlineContent = { Text(checklist.name) }, trailingContent = {
                        IconButton(onClick = {
                            coroutineScope.launch { try { checklist.id?.let { RetrofitClient.api.restoreChecklist(auth(token), it) }; notify("Checklist restored"); load() } catch (e: Exception) { notify(extractErrorMessage(e)) } }
                        }) { Icon(Icons.Default.Restore, "Restore") }
                    })
                }
                2 -> items(trades) { trade ->
                    ListItem(headlineContent = { Text(trade.symbol) }, supportingContent = { Text("${trade.status} • ${trade.profitLoss ?: 0.0}") }, trailingContent = {
                        IconButton(onClick = {
                            coroutineScope.launch { try { trade.id?.let { RetrofitClient.api.restoreTrade(auth(token), it) }; notify("Trade restored"); load() } catch (e: Exception) { notify(extractErrorMessage(e)) } }
                        }) { Icon(Icons.Default.Restore, "Restore") }
                    })
                }
            }
        }
    }
}

@Composable
private fun EnumMenu(label: String, value: String, values: List<String>, onValue: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("$label: $value", maxLines = 1, overflow = TextOverflow.Ellipsis) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onValue(option); expanded = false })
            }
        }
    }
}
