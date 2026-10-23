package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import com.example.util.ImageHelper
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.StoreCategories
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderStrong
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUploadAppSheet(
    onDismiss: () -> Unit,
    onCopyApkFromUri: (Uri, String) -> Pair<String, Long>?,
    onCopyImageFromUri: (Uri, Boolean) -> String?,
    onCreateSampleApk: (String) -> Pair<String, Long>,
    onUploadImageToCloud: ((Uri, Boolean, (String?) -> Unit) -> Unit)? = null,
    onUploadApkToCloud: ((Uri, String, (String?, Long) -> Unit) -> Unit)? = null,
    onUploadSampleApkToCloud: ((String, (String?, Long) -> Unit) -> Unit)? = null,
    supabaseConfig: com.example.data.SupabaseConfig? = null,
    onSaveSupabaseConfig: ((String, String) -> Unit)? = null,
    onUpload: (
        name: String,
        category: String,
        developer: String,
        tagline: String,
        description: String,
        apkFilePath: String,
        sizeBytes: Long,
        version: String,
        iconPath: String,
        screenshots: List<String>,
        bundleId: String,
        price: Double,
        isSubscription: Boolean,
        isTopApp: Boolean,
        isTopGame: Boolean,
        isNewNoteworthy: Boolean,
        isFeatured: Boolean,
        overview: String,
        speedScore: String,
        securityStatus: String,
        cloudApkUrl: String
    ) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onDismiss() }

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Productivity") }
    var developer by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("1.0.0") }
    var tagline by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bundleId by remember { mutableStateOf("") }

    // APK state - directly from file or online cloud URL
    var apkPath by remember { mutableStateOf("") }
    var cloudApkUrlInput by remember { mutableStateOf("") }
    var apkFileName by remember { mutableStateOf("") }
    var apkSizeBytes by remember { mutableStateOf(0L) }

    // Media states
    var iconPath by remember { mutableStateOf("") }
    val screenshots = remember { mutableStateListOf<String>() }

    // Highlights
    var overviewText by remember { mutableStateOf("Native high-performance mobile architecture.") }
    var speedScoreText by remember { mutableStateOf("99/100") }
    var securityStatusText by remember { mutableStateOf("Verified Sandboxed") }

    // Placements
    var isTopApp by remember { mutableStateOf(false) }
    var isTopGame by remember { mutableStateOf(false) }
    var isFeatured by remember { mutableStateOf(false) }
    var isNewNoteworthy by remember { mutableStateOf(true) }

    // Pricing & Subscription
    var isSubscription by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("0.0") }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf("") }
    var showSupabaseSettingsDialog by remember { mutableStateOf(false) }

    // APK File Picker Launcher
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "application.apk"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            apkFileName = fileName

            if (onUploadApkToCloud != null) {
                isUploadingMedia = true
                uploadStatusMessage = "Uploading APK to Supabase Storage ($fileName)..."
                onUploadApkToCloud(uri, fileName) { uploadedUrl, sizeBytes ->
                    isUploadingMedia = false
                    uploadStatusMessage = ""
                    if (!uploadedUrl.isNullOrBlank()) {
                        apkPath = uploadedUrl
                        cloudApkUrlInput = uploadedUrl
                        apkSizeBytes = sizeBytes
                    } else {
                        val result = onCopyApkFromUri(uri, fileName)
                        if (result != null) {
                            apkPath = result.first
                            apkSizeBytes = result.second
                        }
                    }
                }
            } else {
                val result = onCopyApkFromUri(uri, fileName)
                if (result != null) {
                    apkPath = result.first
                    apkSizeBytes = result.second
                }
            }
        }
    }

    // App Icon Picker Launcher
    val iconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onUploadImageToCloud != null) {
                isUploadingMedia = true
                uploadStatusMessage = "Uploading logo to Supabase Storage..."
                onUploadImageToCloud(uri, true) { uploadedUrl ->
                    isUploadingMedia = false
                    uploadStatusMessage = ""
                    if (!uploadedUrl.isNullOrBlank()) {
                        iconPath = uploadedUrl
                    }
                }
            } else {
                val savedPath = onCopyImageFromUri(uri, true)
                if (savedPath != null) {
                    iconPath = savedPath
                }
            }
        }
    }

    // Screenshot Picker Launcher
    val screenshotPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onUploadImageToCloud != null) {
                isUploadingMedia = true
                uploadStatusMessage = "Uploading screenshot to Supabase Storage..."
                onUploadImageToCloud(uri, false) { uploadedUrl ->
                    isUploadingMedia = false
                    uploadStatusMessage = ""
                    if (!uploadedUrl.isNullOrBlank()) {
                        screenshots.add(uploadedUrl)
                    }
                }
            } else {
                val savedPath = onCopyImageFromUri(uri, false)
                if (savedPath != null) {
                    screenshots.add(savedPath)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = { /* No-op: full page */ },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Upload Real App to Store",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Upload actual APK file, screenshots, and metadata",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("admin_upload_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    modifier = Modifier.statusBarsPadding()
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .testTag("admin_upload_full_page")
            ) {

            Spacer(modifier = Modifier.height(14.dp))

            // Supabase Cloud Storage Status Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF10B981).copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Supabase Storage: Enabled",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Logos, screenshots & APKs are public for all users",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    TextButton(onClick = { showSupabaseSettingsDialog = true }) {
                        Text("Config", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isUploadingMedia) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uploadStatusMessage.ifBlank { "Uploading media to Supabase Storage..." },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceSecondary,
                border = BorderStroke(1.dp, if (apkPath.isNotBlank()) AppleBlue else BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (apkPath.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = if (apkPath.isNotBlank()) Color(0xFF248A4B) else AppleBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "APK File (.apk)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        if (apkSizeBytes > 0) {
                            val mb = apkSizeBytes.toDouble() / (1024 * 1024)
                            val formatted = if (mb < 1.0) "${apkSizeBytes / 1024} KB" else String.format("%.1f MB", mb)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = formatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (apkPath.isNotBlank()) {
                        Text(
                            text = "Selected: $apkFileName",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                        Text(
                            text = "Size automatically set from actual file: ${(apkSizeBytes / (1024 * 1024.0)).let { String.format("%.1f MB", it) }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "Provide a direct Cloud APK URL (so any user can download online) or pick an APK file from phone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cloudApkUrlInput,
                        onValueChange = { url ->
                            cloudApkUrlInput = url
                            if (url.isNotBlank()) {
                                apkPath = url.trim()
                                apkFileName = url.substringAfterLast("/").ifBlank { "app.apk" }
                                if (apkSizeBytes <= 0) apkSizeBytes = 25L * 1024 * 1024
                            }
                        },
                        label = { Text("Cloud Download URL (HTTPS Link to APK)") },
                        placeholder = { Text("https://example.com/app-release.apk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_cloud_apk_url")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { apkPickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("select_apk_file_btn")
                        ) {
                            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (apkPath.isBlank()) "Choose Local APK" else "Pick File")
                        }

                        OutlinedButton(
                            onClick = {
                                val testName = name.ifBlank { "SampleApp" }
                                if (onUploadSampleApkToCloud != null) {
                                    isUploadingMedia = true
                                    uploadStatusMessage = "Creating & uploading sample to Supabase..."
                                    onUploadSampleApkToCloud(testName) { uploadedUrl, size ->
                                        isUploadingMedia = false
                                        uploadStatusMessage = ""
                                        if (!uploadedUrl.isNullOrBlank()) {
                                            apkPath = uploadedUrl
                                            cloudApkUrlInput = uploadedUrl
                                            apkSizeBytes = size
                                            apkFileName = "${testName.lowercase()}_release.apk"
                                        }
                                    }
                                } else {
                                    val result = onCreateSampleApk(testName)
                                    apkPath = result.first
                                    apkSizeBytes = result.second
                                    apkFileName = "${testName.lowercase()}_release.apk"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("generate_test_apk_btn")
                        ) {
                            Text(text = "Sample Package", color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. APP SPECIFICATION FIELDS
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("App Name *") },
                placeholder = { Text("e.g. Pixel Forge") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_input_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Specific Version *") },
                    placeholder = { Text("1.0.0") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("admin_input_version")
                )

                OutlinedTextField(
                    value = developer,
                    onValueChange = { developer = it },
                    label = { Text("Developer *") },
                    placeholder = { Text("Developer / Studio") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("admin_input_developer")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. EXPANDED CATEGORY SELECTOR (30+ Categories + Swipe Down + Custom Entry)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Category (30+ Available or Custom)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "Swipe down list",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AppleBlue
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderStrong),
                    color = SurfaceLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDialog = true }
                        .testTag("admin_category_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = category, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(text = "Tap to choose from 30+ categories or type custom", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("Productivity / Tagline *") },
                placeholder = { Text("Short snappy subtitle") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_input_tagline")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Full App Description *") },
                placeholder = { Text("Explain what the app does, key tools, and why users should download it...") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth().testTag("admin_input_description")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. OVERVIEW, SPEED & SECURITY SECTION
            Text(
                text = "Overview, Speed & Security Specifications",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = overviewText,
                onValueChange = { overviewText = it },
                label = { Text("Architectural Overview") },
                placeholder = { Text("Native package overview & design...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = speedScoreText,
                    onValueChange = { speedScoreText = it },
                    label = { Text("Speed Score") },
                    placeholder = { Text("99/100") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = securityStatusText,
                    onValueChange = { securityStatusText = it },
                    label = { Text("Security Status") },
                    placeholder = { Text("Verified Sandboxed") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. PICTURES FROM INSIDE THE APP (Screenshots & Icon)
            Text(
                text = "Pictures From Inside The App & Icon",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // App Icon Picker
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSecondary,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (iconPath.isNotBlank()) {
                            AsyncImage(
                                model = ImageHelper.buildImageModel(iconPath) ?: iconPath,
                                contentDescription = "App Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.08f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Icon", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(text = if (iconPath.isBlank()) "Choose logo image" else "Custom icon selected", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }

                    OutlinedButton(
                        onClick = { iconPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_pick_icon_button")
                    ) {
                        Text(text = if (iconPath.isBlank()) "Select" else "Change", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = iconPath,
                onValueChange = { iconPath = it.trim() },
                label = { Text("App Icon URL or Base64 (from device or Supabase/Web)") },
                placeholder = { Text("https://... or auto-filled when selected above") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_input_icon_url")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Screenshots Gallery Picker
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceSecondary,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Inside App Screenshots (${screenshots.size})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Add pictures of the app so users can zoom in and inspect",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { screenshotPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_add_screenshot_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Add Photo", fontSize = 12.sp)
                        }
                    }

                    if (screenshots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            screenshots.forEachIndexed { index, path ->
                                Box(modifier = Modifier.size(70.dp, 100.dp)) {
                                    AsyncImage(
                                        model = ImageHelper.buildImageModel(path) ?: path,
                                        contentDescription = "Screenshot $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .clickable { screenshots.removeAt(index) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var directScreenshotUrl by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = directScreenshotUrl,
                            onValueChange = { directScreenshotUrl = it },
                            label = { Text("Or Paste Screenshot URL (Supabase/Web)") },
                            placeholder = { Text("https://...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (directScreenshotUrl.isNotBlank()) {
                                    screenshots.add(directScreenshotUrl.trim())
                                    directScreenshotUrl = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            enabled = directScreenshotUrl.isNotBlank()
                        ) {
                            Text("Add URL", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. SPECIFICATION TAGS (Top Apps, Top Games, Featured)
            Text(text = "Store Placement Specifications", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isTopApp, onCheckedChange = { isTopApp = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Black))
                Text(text = "Mark as Top App (Appears in Top Apps feed)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isTopGame, onCheckedChange = { isTopGame = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Black))
                Text(text = "Mark as Top Game (Appears in Top Games feed)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Black))
                Text(text = "Mark as Featured (Top Banner highlight)", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. SUBSCRIPTION & PURCHASE SETTINGS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSubscription, onCheckedChange = { isSubscription = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Black))
                Text(text = "Requires Subscription / Purchase", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }

            if (isSubscription) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (USD)") },
                    placeholder = { Text("4.99") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (name.isNotBlank() && apkPath.isNotBlank()) {
                        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                        val resolvedCloudUrl = cloudApkUrlInput.trim().ifBlank {
                            if (apkPath.startsWith("http://", ignoreCase = true) || apkPath.startsWith("https://", ignoreCase = true)) {
                                apkPath
                            } else {
                                ""
                            }
                        }
                        onUpload(
                            name.trim(),
                            category.trim(),
                            developer.trim().ifBlank { "Independent Developer" },
                            tagline.trim().ifBlank { "Verified Android Application" },
                            description.trim().ifBlank { "High-speed clean application published to Spoiled Store." },
                            apkPath,
                            apkSizeBytes,
                            version.trim().ifBlank { "1.0.0" },
                            iconPath,
                            screenshots.toList(),
                            bundleId.trim().ifBlank { "com.spoiled.${name.lowercase().replace("\\s+".toRegex(), "_")}" },
                            parsedPrice,
                            isSubscription,
                            isTopApp,
                            isTopGame,
                            isNewNoteworthy,
                            isFeatured,
                            overviewText,
                            speedScoreText,
                            securityStatusText,
                            resolvedCloudUrl
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                enabled = name.isNotBlank() && apkPath.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("admin_upload_app_submit_btn")
            ) {
                Text(
                    text = if (apkPath.isBlank()) "Select APK First" else "Publish App to Spoiled Store",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // 30+ CATEGORIES SCROLLABLE / SWIPEABLE SELECTION DIALOG
    if (showCategoryDialog) {
        var categorySearch by remember { mutableStateOf("") }
        var customCategoryInput by remember { mutableStateOf("") }

        val filteredCategories = StoreCategories.ALL_CATEGORIES.filter {
            it.contains(categorySearch, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(22.dp),
            title = {
                Column {
                    Text(
                        text = "Select Category",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Swipe down through 30+ categories or type a custom one",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = categorySearch,
                        onValueChange = { categorySearch = it },
                        placeholder = { Text("Filter 30+ categories...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Custom category creator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = { customCategoryInput = it },
                            placeholder = { Text("Type custom category...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (customCategoryInput.isNotBlank()) {
                                    category = customCategoryInput.trim()
                                    showCategoryDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            enabled = customCategoryInput.isNotBlank()
                        ) {
                            Text("Use")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Swipe-down vertical list of all 30+ official categories
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(filteredCategories) { cat ->
                            val isSelected = cat.equals(category, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color.Black.copy(alpha = 0.05f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        category = cat
                                        showCategoryDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showCategoryDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // SUPABASE STORAGE CONFIGURATION DIALOG
    if (showSupabaseSettingsDialog) {
        var projectUrlInput by remember { mutableStateOf(supabaseConfig?.projectUrl ?: com.example.data.SupabaseStorageManager.DEFAULT_PROJECT_URL) }
        var anonKeyInput by remember { mutableStateOf(supabaseConfig?.anonKey ?: com.example.data.SupabaseStorageManager.DEFAULT_ANON_KEY) }

        AlertDialog(
            onDismissRequest = { showSupabaseSettingsDialog = false },
            containerColor = SurfaceLight,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Supabase Cloud Storage",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Real online storage for app icons, inside-app screenshots, and APK downloads across all user devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = projectUrlInput,
                        onValueChange = { projectUrlInput = it },
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://xyz.supabase.co") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = anonKeyInput,
                        onValueChange = { anonKeyInput = it },
                        label = { Text("Supabase Anon / Public Key") },
                        placeholder = { Text("eyJ...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Active Storage Buckets:\n• app-icons (public read)\n• app-screenshots (public read)\n• app-apks (public read)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveSupabaseConfig?.invoke(projectUrlInput.trim(), anonKeyInput.trim())
                        showSupabaseSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Config")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSupabaseSettingsDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
        }
    }
}
}


