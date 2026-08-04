package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
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
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                TextButton(onClick = { viewModel.deleteShopItem(item); showDeleteConfirm = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
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
    val isPurchased = item.isPurchased

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (!isPurchased) viewModel.toggleShopItemPurchased(item) },
                onLongClick = { showMenu = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPurchased)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isPurchased)
                MaterialTheme.colorScheme.outline.copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.name,
                        fontSize = 15.sp,
                        fontWeight = if (isPurchased) FontWeight.Normal else FontWeight.Medium,
                        color = if (isPurchased)
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        else
                            MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isPurchased) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (item.quantity > 1) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPurchased)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                "${item.quantity}×",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPurchased)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.price != null) {
                        if (item.quantity > 1) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$${String.format("%.2f", item.price)}",
                                fontSize = 12.sp,
                                color = if (isPurchased)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "= $${String.format("%.2f", item.price * item.quantity)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPurchased)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$${String.format("%.2f", item.price)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPurchased)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (item.notes.isNotBlank()) {
                    Text(
                        item.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isPurchased) 0.3f else 0.65f
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (isPurchased) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Purchased",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { viewModel.toggleShopItemPurchased(item) },
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                ) { }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.MoreVert, contentDescription = "More",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit", fontSize = 14.sp) },
                        onClick = { showMenu = false; onEdit(item) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit, contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isPurchased) "Mark to Buy" else "Mark Purchased", fontSize = 14.sp) },
                        onClick = { showMenu = false; viewModel.toggleShopItemPurchased(item) },
                        leadingIcon = {
                            Icon(
                                if (isPurchased) Icons.Default.Undo else Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", fontSize = 14.sp, color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete(item) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete, contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
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
    var isSaving by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val isEdit = initialName.isNotEmpty()
    val nameTrimmed = name.trim()
    val parsedPrice = remember(priceText) {
        val clean = priceText.replace(",", ".").filter { c -> c.isDigit() || c == '.' }
        val dots = clean.count { it == '.' }
        val numeric = if (dots <= 1 && clean.isNotBlank()) clean.toFloatOrNull()?.takeIf { it >= 0f } else null
        numeric
    }
    val isValid = nameTrimmed.isNotEmpty() && !isSaving

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (isEdit) "Edit Item" else "Add to List",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        if (isEdit) "Update item details" else "Add something you need to buy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(100) },
                    label = { Text("Item name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    supportingText = {
                        if (name.length >= 90) {
                            Text(
                                "${name.length}/100",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Quantity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (quantity > 1) {
                                        quantity--
                                        qtyText = quantity.toString()
                                    }
                                },
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            OutlinedTextField(
                                value = qtyText,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    if (filtered.length <= 5) {
                                        qtyText = filtered
                                        if (filtered.isNotEmpty()) {
                                            quantity = filtered.toInt().coerceAtLeast(1)
                                        }
                                    }
                                },
                                modifier = Modifier.width(64.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            FilledTonalIconButton(
                                onClick = {
                                    quantity++
                                    qtyText = quantity.toString()
                                },
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { input ->
                            val cleaned = input.filter { c ->
                                c.isDigit() || c == '.' || c == ','
                            }
                            val dots = cleaned.count { it == '.' || it == ',' }
                            val afterDecimal = if (dots <= 1) cleaned else {
                                val firstDot = cleaned.indexOfFirst { it == '.' || it == ',' }
                                cleaned.substring(0, firstDot + 1) +
                                        cleaned.substring(firstDot + 1).filter { it.isDigit() }
                            }
                            val withLimit = if (afterDecimal.contains(".") || afterDecimal.contains(",")) {
                                val dotIdx = maxOf(
                                    afterDecimal.indexOfFirst { it == '.' || it == ',' },
                                    0
                                )
                                val before = afterDecimal.substring(0, dotIdx).take(6)
                                val after = afterDecimal.substring(dotIdx + 1).take(2)
                                val sep = afterDecimal[dotIdx]
                                "$before$sep$after"
                            } else {
                                afterDecimal.take(6)
                            }
                            priceText = withLimit
                            priceError = false
                        },
                        label = { Text("Unit price") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        prefix = {
                            Text(
                                "$",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        },
                        isError = priceError,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    if (parsedPrice != null && quantity > 0) {
                        val total = parsedPrice * quantity
                        Surface(
                            modifier = Modifier.padding(top = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Total",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "$${String.format("%.2f", total)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(200) },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    supportingText = {
                        Text(
                            "${notes.length}/200",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (notes.length > 180) 0.7f else 0.35f
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    val qty = if (qtyText.isNotBlank()) qtyText.toInt().coerceAtLeast(1) else 1
                    val price = if (priceText.isNotBlank()) parsedPrice else null
                    if (priceText.isNotBlank() && parsedPrice == null) {
                        priceError = true
                        isSaving = false
                        return@Button
                    }
                    onConfirm(nameTrimmed, qty, price, notes.trim())
                },
                enabled = isValid,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}
