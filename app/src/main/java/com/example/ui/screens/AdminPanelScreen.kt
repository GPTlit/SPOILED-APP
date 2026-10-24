package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.models.AppItem
import com.example.data.models.FeaturedBannerConfig
import com.example.data.models.NewsItem
import com.example.ui.components.AppIconImage
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.components.motionEntrance
import com.example.ui.components.pressBounce
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AdminPanelScreen(
    apps: List<AppItem>,
    newsList: List<NewsItem>,
    adminEmail: String,
    onBackClick: () -> Unit,
    onOpenUploadDialog: () -> Unit,
    onOpenUploadNewsDialog: () -> Unit,
    onOpenBroadcastNotificationDialog: () -> Unit,
    onDeleteApp: (String) -> Unit,
    onDeleteNews: (String) -> Unit,
    onAppClick: (String) -> Unit,
    featuredBanner: FeaturedBannerConfig = FeaturedBannerConfig(),
    onUpdateFeaturedBanner: (title: String, tagline: String, imageUrl: String) -> Unit = { _, _, _ -> },
    onDeleteDemoApps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    androidx.activity.compose.BackHandler { onBackClick() }

    var showBannerDialog by remember { mutableStateOf(false) }
    var showDeleteDemoConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_panel_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Admin Panel",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Admin: $adminEmail",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Actions Row (Upload Real APK, Broadcast Notification, Post News)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Publishing & Broadcast Controls",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Upload real APK files, inside app screenshots, 30+ specifications, broadcast store-wide notifications, and publish news.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenUploadDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("admin_upload_app_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Upload App", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }

                        OutlinedButton(
                            onClick = onOpenBroadcastNotificationDialog,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("admin_broadcast_notif_button")
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Send Alert", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                        }

                        OutlinedButton(
                            onClick = onOpenUploadNewsDialog,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("admin_upload_news_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Article, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Post News", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showBannerDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("admin_customize_banner_button")
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Edit Featured Banner", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                        }

                        OutlinedButton(
                            onClick = { showDeleteDemoConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("admin_delete_demo_apps_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Clear Demo Apps", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Apps Catalog Section
        item {
            Text(
                text = "Store Applications (${apps.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (apps.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No apps uploaded yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Click 'Upload App' above to upload your first APK package.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        itemsIndexed(apps, key = { _, it -> it.id }) { index, app ->
            val interactionSource = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .motionEntrance(index = index, delayPerIndexMs = 25)
                    .pressBounce(interactionSource = interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onAppClick(app.slug) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconImage(
                        iconUrl = app.iconUrl,
                        fallbackText = app.iconFallbackText,
                        accentColor = app.iconColorHex,
                        size = 48.dp,
                        cornerRadius = 12.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            if (app.isTopApp) {
                                Spacer(modifier = Modifier.width(4.dp))
                                SpecTag("TOP APP", Color.Black)
                            }
                            if (app.isTopGame) {
                                Spacer(modifier = Modifier.width(4.dp))
                                SpecTag("TOP GAME", Color(0xFF248A4B))
                            }
                            if (app.isFeatured) {
                                Spacer(modifier = Modifier.width(4.dp))
                                SpecTag("FEATURED", Color(0xFFA56A12))
                            }
                        }

                        Text(
                            text = "${app.category} • ${app.sizeFormatted} • v${app.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = { onDeleteApp(app.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // News Articles Section
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Published News Stories (${newsList.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (newsList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No news stories published yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Click 'Post News' above to share an editorial or announcement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        itemsIndexed(newsList, key = { _, it -> it.id }) { index, news ->
            val interactionSource = remember { MutableInteractionSource() }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .motionEntrance(index = index, delayPerIndexMs = 25)
                    .pressBounce(interactionSource = interactionSource)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${news.category} • ${news.date} by ${news.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = { onDeleteNews(news.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Story",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showBannerDialog) {
        var bannerTitle by remember { mutableStateOf(featuredBanner.title) }
        var bannerTagline by remember { mutableStateOf(featuredBanner.tagline) }
        var bannerImageUrl by remember { mutableStateOf(featuredBanner.imageUrl) }

        val bannerPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                bannerImageUrl = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showBannerDialog = false },
            modifier = Modifier.motionEntrance(0),
            title = {
                Text(
                    text = "Edit Featured Ecosystem Banner",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Customize the top hero banner on the store homepage. Admin can change the picture, headline, tagline, or pick an image directly from device storage.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = bannerTagline,
                        onValueChange = { bannerTagline = it },
                        label = { Text("Tagline / Category") },
                        placeholder = { Text("FEATURED ECOSYSTEM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bannerTitle,
                        onValueChange = { bannerTitle = it },
                        label = { Text("Banner Headline") },
                        placeholder = { Text("Real APK Downloads & Cloud Storage") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bannerImageUrl,
                        onValueChange = { bannerImageUrl = it },
                        label = { Text("Artwork Image URL / Path") },
                        placeholder = { Text("https://... or content://... or leave empty for 3D phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { bannerPickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Image", fontSize = 12.sp)
                        }

                        if (bannerImageUrl.isNotBlank()) {
                            TextButton(
                                onClick = { bannerImageUrl = "" },
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("Reset Art", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateFeaturedBanner(bannerTitle, bannerTagline, bannerImageUrl)
                        showBannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Banner")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showBannerDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showDeleteDemoConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDemoConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Demo Catalog Apps?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "This will remove all pre-bundled sample apps from the store catalog so that only real applications uploaded by you (the administrator) will be visible to users.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDemoApps()
                        showDeleteDemoConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete Demo Apps")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDemoConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SpecTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                color = color
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}
