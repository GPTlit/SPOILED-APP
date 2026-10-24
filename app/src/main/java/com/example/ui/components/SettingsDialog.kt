package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SettingsDialog(
    currentAdminEmail: String,
    themePreference: String = "dark",
    onSetThemePreference: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveAdminEmail: (String) -> Unit
) {
    var adminEmailInput by remember { mutableStateOf(currentAdminEmail) }
    var autoUpdates by remember { mutableStateOf(true) }
    var wifiOnly by remember { mutableStateOf(false) }
    var autoInstall by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var smoothMotion by remember { mutableStateOf(true) }
    var cacheClearedMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .motionEntrance(0, offsetY = 16.dp, initialScale = 0.92f)
            .testTag("settings_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Store Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Preferences, Appearance & Security",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. APPEARANCE / THEME SECTION
                SettingsSectionHeader(
                    icon = Icons.Default.Palette,
                    title = "Appearance"
                )
                Text(
                    text = "Select your preferred color theme. Applies instantly across all store screens.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsThemeChip(
                        label = "Dark Mode",
                        icon = Icons.Default.DarkMode,
                        isSelected = themePreference == "dark",
                        onClick = { onSetThemePreference("dark") },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsThemeChip(
                        label = "Light Mode",
                        icon = Icons.Default.LightMode,
                        isSelected = themePreference == "light",
                        onClick = { onSetThemePreference("light") },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsThemeChip(
                        label = "System",
                        icon = Icons.Default.SettingsBrightness,
                        isSelected = themePreference == "system",
                        onClick = { onSetThemePreference("system") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(16.dp))

                // 2. DOWNLOADS & UPDATES SECTION
                SettingsSectionHeader(
                    icon = Icons.Default.SystemUpdate,
                    title = "Downloads & Installation"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Automatic App Updates",
                    subtitle = "Silently keep installed applications at latest release",
                    checked = autoUpdates,
                    onCheckedChange = { autoUpdates = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Download Over Wi-Fi Only",
                    subtitle = "Prevent large APK downloads from using cellular data",
                    checked = wifiOnly,
                    onCheckedChange = { wifiOnly = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingsToggleRow(
                    title = "Auto-Install After Download",
                    subtitle = "Trigger Android Package Installer immediately when download completes",
                    checked = autoInstall,
                    onCheckedChange = { autoInstall = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. MOTION & PERFORMANCE SECTION
                SettingsSectionHeader(
                    icon = Icons.Default.Speed,
                    title = "Motion & Animations"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Fluid Motion & Animations",
                    subtitle = "Enable smooth 60/120Hz gesture physics and transition choreography",
                    checked = smoothMotion,
                    onCheckedChange = { smoothMotion = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(16.dp))

                // 4. NOTIFICATIONS & ALERTS
                SettingsSectionHeader(
                    icon = Icons.Default.NotificationsActive,
                    title = "Notifications & Announcements"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsToggleRow(
                    title = "Store Broadcast Alerts",
                    subtitle = "Receive notifications about releases, updates, and news",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. STORAGE & CACHE
                SettingsSectionHeader(
                    icon = Icons.Default.DeleteSweep,
                    title = "Storage & Cache"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceSecondary),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Download Cache",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                            Text(
                                text = cacheClearedMessage ?: "Frees temporary downloaded APK files from cache",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (cacheClearedMessage != null) Color(0xFF248A4B) else TextTertiary
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                cacheClearedMessage = "Cache cleared (0 MB)"
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(16.dp))

                // 6. ADMIN SECURITY SECTION
                SettingsSectionHeader(
                    icon = Icons.Default.Security,
                    title = "Admin Email Access Control"
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Accounts signed in with this authorized email will unlock the Admin Panel in the Profile section.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = adminEmailInput,
                    onValueChange = { adminEmailInput = it },
                    label = { Text("Authorized Admin Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("settings_admin_email_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveAdminEmail(adminEmailInput.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("settings_done_button")
            ) {
                Text(text = "Save Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
    }
}

@Composable
private fun SettingsThemeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.Black else SurfaceSecondary,
        border = BorderStroke(1.dp, if (isSelected) Color.Black else BorderLight),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp
                ),
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppleBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SurfaceSecondary
            )
        )
    }
}
