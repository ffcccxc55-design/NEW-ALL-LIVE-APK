package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Channel
import com.example.data.model.PremiumItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Collecting StateFlows from ViewModel
    val channels by viewModel.channels.collectAsState()
    val premiumItems by viewModel.premiumItems.collectAsState()
    val latestFeedback by viewModel.feedback.collectAsState()
    val activeNotification by viewModel.activeNotification.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val visitorProfile by viewModel.visitorProfile.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()
    val adState by viewModel.adState.collectAsState()

    // Modals visibility states
    var showRenameDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showAdminVerifyDialog by remember { mutableStateOf(false) }
    var adFreeKeyInput by remember { mutableStateOf("") }

    // Glow Animation for active live pulse
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Trigger external link opening when ad skipping is completed
    LaunchedEffect(adState.showOverlay, adState.canSkip) {
        if (!adState.showOverlay && adState.targetUrl.isNotBlank()) {
            openExternalUrl(context, adState.targetUrl)
            viewModel.dismissAdOverlay()
        }
    }

    TwilightBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Elegant Glass Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldGlow)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "অল লাইভ পোর্টাল",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showAdminVerifyDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                        .testTag("admin_portal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = CyanGlow
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Live Notification Banner
                activeNotification?.let {
                    ShimmerBanner(
                        text = it.content,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 2. Visitor Profile Card
                visitorProfile?.let { profile ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        borderGlow = true
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Profile",
                                        tint = CyanGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = profile.username,
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                        text = "আইডি: ${profile.userId}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = "Visits",
                                        tint = IndigoGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "মোট ভিজিট: ${profile.visitCount} বার",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { showRenameDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x16FFFFFF),
                                    contentColor = CyanGlow
                                ),
                                border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("rename_button")
                            ) {
                                Text(
                                    text = "নাম পরিবর্তন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. Dynamic Interactive Grid (Channels)
                Text(
                    text = "🎯 সরাসরি লাইভ চ্যানেলসমূহ",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val activeChannels = channels.filter { it.isActive }
                if (activeChannels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো লাইভ লিংক বর্তমানে সক্রিয় নেই।",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Custom adaptive flexbox-like grid layout
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 20.dp)
                    ) {
                        val chunks = activeChannels.chunked(2)
                        chunks.forEach { rowChannels ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowChannels.forEach { channel ->
                                    GlassCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("channel_item_${channel.id}"),
                                        onClick = { viewModel.onChannelClicked(channel) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Emoji Logo
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0x0AFFFFFF))
                                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = channel.logo,
                                                    fontSize = 20.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = channel.name,
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(EmeraldGlow.copy(alpha = pulseAlpha))
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "ক্লিক করুন",
                                                        color = EmeraldGlow,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                // Fill missing slots in last row
                                if (rowChannels.size < 2) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 4. Premium Modifications showcase
                Text(
                    text = "🔥 প্রিমিয়াম মড অ্যাপ্লিকেশন",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (premiumItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "কোনো প্রিমিয়াম অ্যাপ্লিকেশন আপলোড করা হয়নি।",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        items(premiumItems) { item ->
                            PremiumModCard(
                                item = item,
                                onClick = { openExternalUrl(context, item.link) }
                            )
                        }
                    }
                }

                // 5. Developer Card (Md Hasan Khalifa)
                config?.let { cfg ->
                    Text(
                        text = "👨‍💻 ডেভেলপারের তথ্য",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(CyanGlow, IndigoGlow))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "MHK",
                                    color = Slate950,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = cfg.devName,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "অফিসিয়াল অ্যাপ ডেভেলপার",
                                    color = CyanGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = cfg.devDescription,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val waUrl = "https://wa.me/${cfg.devWhatsApp}"
                                    openExternalUrl(context, waUrl)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPhone,
                                        contentDescription = "WhatsApp",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { openExternalUrl(context, cfg.devFacebook) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1877F2),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Facebook",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Facebook",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. User Feedback / Reviews Section Teaser
                Text(
                    text = "💬 সাম্প্রতিক ভিজিটর মন্তব্য",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    if (latestFeedback.isEmpty()) {
                        Text(
                            text = "এখনও কোনো মন্তব্য জমা পড়েনি। আপনার মূল্যবান মতামত প্রথম শেয়ার করুন!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        latestFeedback.take(3).forEachIndexed { index, fb ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = fb.username,
                                        color = CyanGlow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val formattedTime = java.text.SimpleDateFormat(
                                        "hh:mm a, dd MMM",
                                        java.util.Locale.getDefault()
                                    ).format(java.util.Date(fb.timestamp))
                                    Text(
                                        text = formattedTime,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = fb.message,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                if (index < latestFeedback.take(3).size - 1) {
                                    Divider(
                                        color = GlassBorder,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showFeedbackDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("open_feedback_dialog")
                    ) {
                        Text(
                            text = "মতামত বা কমেন্ট লিখুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 7. Bypass Key input (Ad block)
                Text(
                    text = "🚫 বিজ্ঞাপন মুক্ত ব্রাউজিং",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp)
                ) {
                    if (isAdFree) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "বিজ্ঞাপন মুক্ত করা হয়েছে (AD-FREE!)",
                                    color = EmeraldGlow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(onClick = { viewModel.removeAdFree() }) {
                                Text("রিসেট করুন", color = CrimsonAlert, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "AD-FREE কোড থাকলে এখানে প্রবেশ করুন। এটি বিজ্ঞাপন ছাড়া সরাসরি লিংকগুলো লোড করবে।",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = adFreeKeyInput,
                                onValueChange = { adFreeKeyInput = it },
                                placeholder = { Text("যেমন: ADFREE_HASAN", color = TextSecondary, fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    unfocusedBorderColor = Slate800,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("ad_free_input")
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    val success = viewModel.submitAdFreeKey(adFreeKeyInput)
                                    if (success) {
                                        Toast.makeText(context, "অভিনন্দন! বিজ্ঞাপন মুক্ত করা হয়েছে।", Toast.LENGTH_SHORT).show()
                                        adFreeKeyInput = ""
                                    } else {
                                        Toast.makeText(context, "ভুল কোড! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("ad_free_submit")
                            ) {
                                Text("ক্লেম", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Modals / Overlays
        if (showRenameDialog) {
            RenameDialog(
                currentName = visitorProfile?.username ?: "",
                onDismiss = { showRenameDialog = false },
                onSave = { newName -> viewModel.updateVisitorUsername(newName) }
            )
        }

        if (showFeedbackDialog) {
            FeedbackDialog(
                onDismiss = { showFeedbackDialog = false },
                onSubmit = { feedbackMsg ->
                    viewModel.submitUserFeedback(feedbackMsg)
                    Toast.makeText(context, "ধন্যবাদ! আপনার মতামত সফলভাবে জমা হয়েছে।", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAdminVerifyDialog) {
            config?.let { cfg ->
                AdminVerifyDialog(
                    securityQuestion = cfg.securityQuestion,
                    onDismiss = { showAdminVerifyDialog = false },
                    onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) },
                    onVerifySecurityAnswer = { ans -> viewModel.verifySecurityAnswer(ans) },
                    onSuccess = {
                        showAdminVerifyDialog = false
                        onNavigateToAdmin()
                    }
                )
            }
        }

        // Skip Ad Overlay Player
        if (adState.showOverlay) {
            VideoAdOverlay(
                countdown = adState.countdown,
                canSkip = adState.canSkip,
                onSkip = { viewModel.dismissAdOverlay() }
            )
        }
    }
}

@Composable
fun PremiumModCard(
    item: PremiumItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x18FFFFFF))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.logo,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.version,
                        color = IndigoGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(32.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.size,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("download_mod_${item.id}")
                ) {
                    Text(
                        text = "ডাউনলোড",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "লিংকটি ওপেন করা সম্ভব হয়নি!", Toast.LENGTH_SHORT).show()
    }
}
