package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.ShopItemEntity
import com.example.ui.viewmodel.MainViewModel

private enum class ShopFilter { ALL, TO_BUY, PURCHASED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopListScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allItems by viewModel.allShopItems.collectAsState()
    val unpurchased by viewModel.unpurchasedItems.collectAsState()
    val purchased by viewModel.purchasedItems.collectAsState()

    var filter by remember { mutableStateOf(ShopFilter.TO_BUY) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShopItemEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<ShopItemEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val displayItems = when (filter) {
        ShopFilter.ALL -> allItems
        ShopFilter.TO_BUY -> unpurchased
        ShopFilter.PURCHASED -> purchased
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SHOP LIST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Shop List",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
            IconButton(onClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) }) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { showSettingsDialog = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShopFilter.entries.forEach { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = {
                        Text(
                            when (f) {
                                ShopFilter.ALL -> "All (${allItems.size})"
                                ShopFilter.TO_BUY -> "To Buy (${unpurchased.size})"
                                ShopFilter.PURCHASED -> "Purchased (${purchased.size})"
                            },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        if (displayItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Your shopping list is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        "Add items you plan to buy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(displayItems, key = { it.id }) { item ->
                    ShopItemCard(
                        item = item,
                        viewModel = viewModel,
                        onEdit = { editingItem = it },
                        onDelete = { showDeleteConfirm = it }
                    )
                }
            }
        }
    }

    if (showAddDialog || editingItem != null) {
        val existing = editingItem
        AddEditShopItemDialog(
            initialName = existing?.name ?: "",
            initialQuantity = existing?.quantity ?: 1,
            initialPrice = existing?.price,
            initialNotes = existing?.notes ?: "",
            onDismiss = { showAddDialog = false; editingItem = null },
            onConfirm = { name, quantity, price, notes ->
                if (existing != null) {
                    viewModel.updateShopItem(existing.copy(name = name, quantity = quantity.coerceAtLeast(1), price = price?.takeIf { it >= 0f }, notes = notes))
                } else {
                    viewModel.addShopItem(name, quantity, price, notes)
                }
                showAddDialog = false; editingItem = null
            }
        )
    }
    showDeleteConfirm?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Item", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${item.name}\"?", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteShopItem(item); showDeleteConfirm = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShopItemCard(
    item: ShopItemEntity,
    viewModel: MainViewModel,
    onEdit: (ShopItemEntity) -> Unit,
    onDelete: (ShopItemEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { if (!item.isPurchased) viewModel.toggleShopItemPurchased(item) },
            onLongClick = { showMenu = true }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPurchased) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (item.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${item.quantity}× ${item.name}",
                        fontSize = 14.sp,
                        fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.Medium,
                        color = if (item.isPurchased) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.notes.isNotBlank()) {
                    Text(
                        item.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                    )
                }
            }
            if (item.price != null) {
                Text(
                    "$${String.format("%.2f", item.price)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Checkbox(
                checked = item.isPurchased,
                onCheckedChange = { viewModel.toggleShopItemPurchased(item) },
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(item) })
                    DropdownMenuItem(
                        text = { Text(if (item.isPurchased) "Mark to Buy" else "Mark Purchased") },
                        onClick = { showMenu = false; viewModel.toggleShopItemPurchased(item) }
                    )
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete(item) })
                }
            }
        }
    }
}

@Composable
private fun AddEditShopItemDialog(
    initialName: String,
    initialQuantity: Int,
    initialPrice: Float?,
    initialNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Float?, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableIntStateOf(initialQuantity) }
    var qtyText by remember { mutableStateOf(initialQuantity.toString()) }
    var priceText by remember { mutableStateOf(initialPrice?.let { String.format("%.2f", it) } ?: "") }
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (initialName.isNotEmpty()) "Edit Item" else "Add Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Qty:", fontSize = 14.sp, modifier = Modifier.width(40.dp))
                    IconButton(onClick = { if (quantity > 1) { quantity--; qtyText = quantity.toString() } }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 9) {
                                qtyText = filtered
                                if (filtered.isNotEmpty()) {
                                    quantity = filtered.toInt().coerceAtLeast(1)
                                }
                            }
                        },
                        modifier = Modifier.width(72.dp)
                            .onFocusChanged { if (!it.isFocused) { val p = qtyText.toIntOrNull() ?: 1; quantity = p.coerceAtLeast(1); qtyText = quantity.toString() } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = { quantity++; qtyText = quantity.toString() }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toFloatOrNull()?.takeIf { it >= 0f }
                    onConfirm(name.trim(), quantity, price, notes.trim())
                },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
