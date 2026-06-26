package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.Channel
import com.example.data.model.NotificationBanner
import com.example.data.model.PremiumItem
import com.example.ui.components.GlassCard
import com.example.ui.components.SyncState
import com.example.ui.components.TwilightBackground
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun AdminScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // State references from ViewModel
    val channels by viewModel.channels.collectAsState()
    val premiumItems by viewModel.premiumItems.collectAsState()
    val feedbacks by viewModel.feedback.collectAsState()
    val logs by viewModel.visitorLogs.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isAdminVerified by viewModel.isAdminAuthenticated.collectAsState()

    // Redirect to home if somehow not authenticated
    LaunchedEffect(isAdminVerified) {
        if (!isAdminVerified) {
            onNavigateBack()
        }
    }

    // Tab state (0: Config, 1: Channels, 2: Premium, 3: Alerts, 4: Dev Card, 5: Logs/Feedback)
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("⚙️ সেটআপ", "📺 চ্যানেল", "🔥 প্রিমিয়াম", "📢 নোটিশ", "👨‍💻 ডেভ কার্ড", "📊 লগ")

    // Handle sync status changes for toast feedbacks
    LaunchedEffect(syncState) {
        when (syncState) {
            is SyncState.Success -> {
                Toast.makeText(context, "অনলাইন সিঙ্ক সফল! ${(syncState as SyncState.Success).count} টি চ্যানেল ডাউনলোড হয়েছে।", Toast.LENGTH_LONG).show()
                viewModel.clearSyncStatus()
            }
            is SyncState.Error -> {
                Toast.makeText(context, "সিঙ্ক ব্যর্থ হয়েছে: ${(syncState as SyncState.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.clearSyncStatus()
            }
            else -> {}
        }
    }

    TwilightBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            viewModel.logoutAdmin()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "অ্যাডমিন ড্যাশবোর্ড",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Log out action badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CrimsonAlert.copy(alpha = 0.2f))
                        .border(1.dp, CrimsonAlert.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.logoutAdmin()
                            onNavigateBack()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "লগ আউট",
                        color = CrimsonAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Beautiful Horizontal Custom Tabs Navigation
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CyanGlow,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanGlow
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) CyanGlow else TextSecondary
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content Router
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> ConfigTab(config ?: AppConfig(), syncState, onSaveConfig = { updated -> viewModel.updateAppConfig(updated) }, onTriggerSync = { sheetId -> viewModel.syncFromGoogleSheets(sheetId) })
                    1 -> ChannelsTab(channels, onAdd = { viewModel.addChannel(it) }, onUpdate = { viewModel.updateChannel(it) }, onDelete = { viewModel.deleteChannel(it) })
                    2 -> PremiumTab(premiumItems, onAdd = { viewModel.addPremiumItem(it) }, onUpdate = { viewModel.updatePremiumItem(it) }, onDelete = { viewModel.deletePremiumItem(it) })
                    3 -> AlertsTab(onAddNotice = { viewModel.addNotification(it) }, onDeleteNotice = { viewModel.deleteNotification(it) })
                    4 -> DevCardTab(config ?: AppConfig(), onSave = { viewModel.updateAppConfig(it) })
                    5 -> LogsTab(logs, feedbacks, onDeleteFeedback = { viewModel.deleteFeedback(it) })
                }
            }
        }
    }
}

// ==================== SUB-TABS LAYOUTS ====================

@Composable
fun ConfigTab(
    config: AppConfig,
    syncState: SyncState,
    onSaveConfig: (AppConfig) -> Unit,
    onTriggerSync: (String) -> Unit
) {
    var sheetIdInput by remember { mutableStateOf(config.googleSheetsId) }
    var pinPrimary by remember { mutableStateOf(config.securityPinPrimary) }
    var pinSecondary by remember { mutableStateOf(config.securityPinSecondary) }
    var resetAns by remember { mutableStateOf(config.securityResetAnswer) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sheet sync dashboard
        GlassCard(borderGlow = true) {
            Text(
                text = "📊 গুগল শিট সিঙ্ক (Google Sheets Sync)",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "একটি গুগল স্প্রেডশিট আইডি যোগ করুন এবং অনলাইনের মাধ্যমে আপনার সমস্ত লাইভ চ্যানেল বাটনগুলো মুহূর্তের মধ্যে ওভাররাইট বা সিঙ্ক করুন।",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = sheetIdInput,
                onValueChange = { sheetIdInput = it },
                label = { Text("Google Spreadsheet ID", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_sheet_id_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onSaveConfig(config.copy(googleSheetsId = sheetIdInput)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = TextPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("আইডি সংরক্ষণ")
                }

                Button(
                    onClick = { onTriggerSync(sheetIdInput) },
                    enabled = syncState !is SyncState.Syncing,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("trigger_sync_button")
                ) {
                    if (syncState is SyncState.Syncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Slate950, strokeWidth = 2.dp)
                    } else {
                        Text("অনলাইন সিঙ্ক করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Security PIN details dashboard
        GlassCard {
            Text(
                text = "🛡️ সিকিউরিটি কোড ও পিন সেটআপ",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pinPrimary,
                onValueChange = { pinPrimary = it },
                label = { Text("Primary PIN (ডিফল্ট: 1234)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pinSecondary,
                onValueChange = { pinSecondary = it },
                label = { Text("Secondary PIN (ডিফল্ট: 5678)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = resetAns,
                onValueChange = { resetAns = it },
                label = { Text("নিরাপত্তা উত্তর: প্রিয় রঙ? (ডিফল্ট: নীল)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    onSaveConfig(
                        config.copy(
                            securityPinPrimary = pinPrimary,
                            securityPinSecondary = pinSecondary,
                            securityResetAnswer = resetAns
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("পিন ও কোড আপডেট করুন", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChannelsTab(
    channels: List<Channel>,
    onAdd: (Channel) -> Unit,
    onUpdate: (Channel) -> Unit,
    onDelete: (Channel) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var linkInput by remember { mutableStateOf("") }
    var logoInput by remember { mutableStateOf("📺") }
    var isEditing by remember { mutableStateOf<Channel?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Creation/Edit Form Card
        GlassCard(borderGlow = isEditing != null) {
            Text(
                text = if (isEditing == null) "➕ নতুন চ্যানেল যুক্ত করুন" else "✏️ চ্যানেল সংশোধন করুন",
                color = if (isEditing == null) CyanGlow else IndigoGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = logoInput,
                    onValueChange = { logoInput = it },
                    label = { Text("ইমোজি", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .width(75.dp)
                        .testTag("admin_channel_logo")
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("চ্যানেলের নাম", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = Slate800,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_channel_name")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = linkInput,
                onValueChange = { linkInput = it },
                label = { Text("সরাসরি লিংক (URL)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_channel_link")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isEditing != null) {
                    Button(
                        onClick = {
                            isEditing = null
                            nameInput = ""
                            linkInput = ""
                            logoInput = "📺"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }
                }

                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && linkInput.isNotBlank()) {
                            if (isEditing == null) {
                                onAdd(Channel(name = nameInput, link = linkInput, logo = logoInput))
                            } else {
                                onUpdate(isEditing!!.copy(name = nameInput, link = linkInput, logo = logoInput))
                                isEditing = null
                            }
                            nameInput = ""
                            linkInput = ""
                            logoInput = "📺"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("admin_save_channel")
                ) {
                    Text(if (isEditing == null) "যোগ করুন" else "আপডেট করুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LazyList of Channels
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(channels) { channel ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x0AFFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(channel.logo, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(channel.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(channel.link, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Active status switch
                            Switch(
                                checked = channel.isActive,
                                onCheckedChange = { active -> onUpdate(channel.copy(isActive = active)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldGlow,
                                    checkedTrackColor = EmeraldGlow.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )

                            IconButton(
                                onClick = {
                                    isEditing = channel
                                    nameInput = channel.name
                                    linkInput = channel.link
                                    logoInput = channel.logo
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanGlow, modifier = Modifier.size(18.dp))
                            }

                            IconButton(onClick = { onDelete(channel) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper expansion function to scale switch
fun Modifier.scale(scale: Float) = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout((placeable.width * scale).toInt(), (placeable.height * scale).toInt()) {
            placeable.placeRelative(
                ((placeable.width * scale - placeable.width) / 2).toInt(),
                ((placeable.height * scale - placeable.height) / 2).toInt()
            )
        }
    }
)

@Composable
fun PremiumTab(
    items: List<PremiumItem>,
    onAdd: (PremiumItem) -> Unit,
    onUpdate: (PremiumItem) -> Unit,
    onDelete: (PremiumItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var logo by remember { mutableStateOf("🎬") }
    var desc by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("v1.0.0") }
    var size by remember { mutableStateOf("45 MB") }
    var isEditing by remember { mutableStateOf<PremiumItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard(borderGlow = isEditing != null) {
            Text(
                text = if (isEditing == null) "➕ নতুন প্রিমিয়াম অ্যাপ যুক্ত করুন" else "✏️ প্রিমিয়াম অ্যাপ সংশোধন করুন",
                color = if (isEditing == null) CyanGlow else IndigoGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = logo,
                    onValueChange = { logo = it },
                    label = { Text("ইমোজি", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    singleLine = true,
                    modifier = Modifier.width(68.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("অ্যাপ্লিকেশনের নাম", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("ভার্সন", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("ফাইল সাইজ", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                    singleLine = true,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("ডাউনলোড লিংক URL", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("সংক্ষিপ্ত বিবরণ", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isEditing != null) {
                    Button(
                        onClick = {
                            isEditing = null
                            name = ""
                            link = ""
                            logo = "🎬"
                            desc = ""
                            version = "v1.0.0"
                            size = "45 MB"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাতিল")
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotBlank() && link.isNotBlank()) {
                            val newItem = PremiumItem(name = name, link = link, logo = logo, description = desc, version = version, size = size)
                            if (isEditing == null) {
                                onAdd(newItem)
                            } else {
                                onUpdate(isEditing!!.copy(name = name, link = link, logo = logo, description = desc, version = version, size = size))
                                isEditing = null
                            }
                            name = ""
                            link = ""
                            logo = "🎬"
                            desc = ""
                            version = "v1.0.0"
                            size = "45 MB"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(if (isEditing == null) "সংরক্ষণ করুন" else "সংশোধন করুন", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(item.logo, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(item.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${item.version} • ${item.size}", color = IndigoGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    isEditing = item
                                    name = item.name
                                    link = item.link
                                    logo = item.logo
                                    desc = item.description
                                    version = item.version
                                    size = item.size
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanGlow, modifier = Modifier.size(18.dp))
                            }

                            IconButton(onClick = { onDelete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertsTab(
    onAddNotice: (String) -> Unit,
    onDeleteNotice: (NotificationBanner) -> Unit
) {
    var noticeText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text(
                text = "📢 নতুন নোটিশ বা সতর্কতা পুশ করুন",
                color = CyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noticeText,
                onValueChange = { noticeText = it },
                label = { Text("ঘোষণা বা সতর্কতার বার্তা লিখুন...", color = TextSecondary) },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (noticeText.isNotBlank()) {
                        onAddNotice(noticeText)
                        noticeText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("পুশ করুন (Live Active Notice)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DevCardTab(
    config: AppConfig,
    onSave: (AppConfig) -> Unit
) {
    var devName by remember { mutableStateOf(config.devName) }
    var devDesc by remember { mutableStateOf(config.devDescription) }
    var devWhatsApp by remember { mutableStateOf(config.devWhatsApp) }
    var devFb by remember { mutableStateOf(config.devFacebook) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard {
            Text(
                text = "👨‍💻 ডেভেলপারের তথ্য পরিবর্তন করুন",
                color = CyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = devName,
                onValueChange = { devName = it },
                label = { Text("ডেভেলপারের নাম", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = devDesc,
                onValueChange = { devDesc = it },
                label = { Text("সংক্ষিপ্ত পরিচিতি", color = TextSecondary) },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = devWhatsApp,
                onValueChange = { devWhatsApp = it },
                label = { Text("WhatsApp ফোন (যেমন: 88017XXXXXXXX)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = devFb,
                onValueChange = { devFb = it },
                label = { Text("Facebook প্রোফাইল লিংক (URL)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanGlow, unfocusedBorderColor = Slate800, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSave(
                        config.copy(
                            devName = devName,
                            devDescription = devDesc,
                            devWhatsApp = devWhatsApp,
                            devFacebook = devFb
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("ডেভেলপার কার্ড সেভ করুন", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LogsTab(
    logs: List<com.example.data.model.VisitorLog>,
    feedbacks: List<com.example.data.model.Feedback>,
    onDeleteFeedback: (com.example.data.model.Feedback) -> Unit
) {
    var viewSelection by remember { mutableStateOf(0) } // 0: Visitor Logs, 1: Feedbacks/Comments

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle view buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewSelection = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewSelection == 0) CyanGlow else Slate800,
                    contentColor = if (viewSelection == 0) Slate950 else TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("📊 ভিজিটর লগ (${logs.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewSelection = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewSelection == 1) CyanGlow else Slate800,
                    contentColor = if (viewSelection == 1) Slate950 else TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("💬 ইউজার কমেন্টস (${feedbacks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (viewSelection == 0) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text("কোনো ভিজিটর লগ পাওয়া যায়নি।", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                } else {
                    items(logs) { log ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(log.username, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("আইডি: ${log.userId}", color = TextSecondary, fontSize = 11.sp)
                                    val time = java.text.SimpleDateFormat("hh:mm a, dd/MM/yy", java.util.Locale.getDefault()).format(java.util.Date(log.lastVisit))
                                    Text("শেষ ভিজিট: $time", color = TextSecondary, fontSize = 11.sp)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x0AFFFFFF))
                                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("${log.visitCount} বার", color = IndigoGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (feedbacks.isEmpty()) {
                    item {
                        Text("কোনো মন্তব্য পাওয়া যায়নি।", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                } else {
                    items(feedbacks) { fb ->
                        GlassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fb.username, color = CyanGlow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    val time = java.text.SimpleDateFormat("hh:mm a, dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(fb.timestamp))
                                    Text(time, color = TextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(fb.message, color = TextPrimary, fontSize = 13.sp)
                                }

                                IconButton(onClick = { onDeleteFeedback(fb) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
