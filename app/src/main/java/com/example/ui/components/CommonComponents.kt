package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun TwilightBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate950, Color(0xFF020617), Color(0xFF0B1329))
                )
            )
    ) {
        // Decorative neon gradient light sources
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1822D3EE), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 350.dp.toPx()
                ),
                radius = 350.dp.toPx(),
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x14818CF8), Color.Transparent),
                    center = Offset(size.width, size.height * 0.6f),
                    radius = 400.dp.toPx()
                ),
                radius = 400.dp.toPx(),
                center = Offset(size.width, size.height * 0.6f)
            )
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderGlow: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val cardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0x221E293B),
            Color(0x120F172A)
        )
    )
    val cardBorder = Brush.linearGradient(
        colors = if (borderGlow) {
            listOf(CyanGlow.copy(alpha = 0.4f), IndigoGlow.copy(alpha = 0.2f))
        } else {
            listOf(GlassBorder, GlassBorder.copy(alpha = 0.05f))
        }
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(cardBackground)
            .border(1.dp, cardBorder, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun ShimmerBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Auto scroll the banner text horizontally for scrolling marquee effect
    LaunchedEffect(text) {
        while (true) {
            scrollState.animateTo(
                value = scrollState.maxValue,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 12000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
            scrollState.scrollTo(0)
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0x3022D3EE), Color(0x30818CF8))))
            .border(1.dp, Color(0x5022D3EE), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Notification",
                tint = CyanGlow,
                modifier = Modifier.padding(end = 8.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState, enabled = false)
            ) {
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.padding(end = 200.dp) // buffer spacing to reset loop
                )
            }
        }
    }
}

@Composable
fun VideoAdOverlay(
    countdown: Int,
    canSkip: Boolean,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant pulsing animation for countdown
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(
        onDismissRequest = {}, // Force user to view or skip
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Slate950)
        ) {
            // Simulated video dynamic visualizer
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3022D3EE), Color.Transparent),
                        center = center,
                        radius = (300f * pulseScale)
                    ),
                    radius = (300f * pulseScale),
                    center = center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Ad Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x30FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "স্পন্সরড বিজ্ঞাপন (ADVERTISEMENT)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Pulsing central dynamic visual circle simulating streaming video player
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(70.dp))
                        .background(Brush.verticalGradient(listOf(Slate900, Slate800)))
                        .border(2.dp, CyanGlow.copy(alpha = 0.6f), RoundedCornerShape(70.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Video playing",
                        tint = CyanGlow,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "প্রিমিয়াম স্পোর্টস লিংক লোড হচ্ছে...",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!canSkip) {
                    Text(
                        text = "⏳ ৫ সেকেন্ড পর বাটনটি লোড হবে... (অবশিষ্ট: $countdown সেকেন্ড)",
                        color = IndigoGlow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "বাটনটি সম্পূর্ণ রেডি হয়ে গেছে!",
                        color = EmeraldGlow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))

                // Skip ad action button
                Button(
                    onClick = { if (canSkip) onSkip() },
                    enabled = canSkip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanGlow,
                        contentColor = Slate950,
                        disabledContainerColor = Slate800,
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                        .testTag("skip_ad_button"),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (canSkip) "বিজ্ঞাপন বন্ধ করুন (Skip Ad)" else "অপেক্ষা করুন...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (canSkip) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Next"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "নাম পরিবর্তন করুন",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("আপনার নাম লিখুন", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = Slate800,
                        focusedLabelColor = CyanGlow,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSave(nameInput)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_name_button")
                    ) {
                        Text("সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var commentInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✍️ মতামত ও কমেন্ট পোর্টাল",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "আপনার যেকোনো সমস্যা বা পরামর্শ এখানে লিখে জমা দিন। অ্যাডমিন এটি সরাসরি মনিটর করবেন।",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    label = { Text("আপনার বার্তা লিখুন...", color = TextSecondary) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = Slate800,
                        focusedLabelColor = CyanGlow,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                onSubmit(commentInput)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_feedback_button")
                    ) {
                        Text("সাবমিট করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVerifyDialog(
    securityQuestion: String,
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onVerifySecurityAnswer: (String) -> Boolean,
    onSuccess: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    var answerInput by remember { mutableStateOf("") }
    var isPinMode by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Slate900)
                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🛡️ অ্যাডমিন সিকিউরিটি গেট",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPinMode) "অ্যাডমিন প্যানেলে প্রবেশ করতে ৪ ডিজিটের কোড দিন।" 
                           else "ভুলে যাওয়া কোড রিকভার করতে নিরাপত্তা প্রশ্নের উত্তর দিন।",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isPinMode) {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { 
                            if (it.length <= 4) {
                                pinInput = it
                                errorMessage = null
                            }
                        },
                        label = { Text("সিকিউরিটি পিন (PIN)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )
                } else {
                    Text(
                        text = "প্রশ্ন: $securityQuestion",
                        color = CyanGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { 
                            answerInput = it
                            errorMessage = null
                        },
                        label = { Text("আপনার উত্তর লিখুন", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_answer_input")
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = CrimsonAlert,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle recovery mode
                TextButton(
                    onClick = { 
                        isPinMode = !isPinMode
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isPinMode) "পাসওয়ার্ড ভুলে গেছেন? (রিকভারি)" else "পিন কোডে ফিরে যান",
                        color = IndigoGlow,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val success = if (isPinMode) {
                                onVerifyPin(pinInput)
                            } else {
                                onVerifySecurityAnswer(answerInput)
                            }

                            if (success) {
                                onSuccess()
                                onDismiss()
                            } else {
                                errorMessage = "ভুল তথ্য প্রদান করেছেন! আবার চেষ্টা করুন।"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Slate950),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_verify_confirm")
                    ) {
                        Text("যাচাই করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
