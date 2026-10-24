package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.models.UserProfile
import com.example.ui.components.ProfileNavigationRow
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.SurfaceLight
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    purchasedCount: Int,
    wishlistCount: Int,
    installedCount: Int = 0,
    unreadNotificationsCount: Int,
    themePreference: String = "dark",
    onSetThemePreference: (String) -> Unit = {},
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPurchasedClick: () -> Unit,
    onSubscriptionsClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onRedeemGiftCardClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isCheckingUpdates by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp)
    ) {
        // 1. Profile Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp
                    ),
                    color = TextPrimary
                )

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("profile_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 2. Account Information Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth().testTag("profile_account_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (userProfile.isSignedIn) Color.Black else Color(0xFFD9DCE2),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (userProfile.isSignedIn) {
                                    Text(
                                        text = userProfile.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (userProfile.isSignedIn) {
                                Text(
                                    text = userProfile.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = userProfile.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (userProfile.isAdmin) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.08f)
                                        ) {
                                            Text(
                                                text = "ADMINISTRATOR",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.5.sp,
                                                    fontSize = 9.sp
                                                ),
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF248A4B).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "Active Session",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            color = Color(0xFF248A4B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Not Signed In",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Sign in to persist your apps, download history, and cloud access forever.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onSignInClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(36.dp).testTag("account_signin_button")
                                ) {
                                    Text(text = "Sign In Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (userProfile.isSignedIn) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderLight, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Provider: ${userProfile.authProvider}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "Persistent Local & Cloud Sync",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = AppleBlue
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. ADMIN PANEL ACCESS (Visible ONLY when user is admin)
        if (userProfile.isAdmin) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.5.dp, Color.Black.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAdminPanelClick() }
                        .testTag("admin_panel_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.08f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Admin Panel",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Upload apps, edit featured banner & manage store",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        // 4. APPEARANCE (Theme Selector: Dark Mode, High Contrast Light Mode, System)
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = TextPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth().testTag("appearance_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Choose your preferred store color theme mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionChip(
                            label = "Dark Mode",
                            icon = Icons.Default.DarkMode,
                            isSelected = themePreference == "dark",
                            onClick = { onSetThemePreference("dark") },
                            modifier = Modifier.weight(1f)
                        )

                        ThemeOptionChip(
                            label = "Light Mode",
                            icon = Icons.Default.LightMode,
                            isSelected = themePreference == "light",
                            onClick = { onSetThemePreference("light") },
                            modifier = Modifier.weight(1f)
                        )

                        ThemeOptionChip(
                            label = "System",
                            icon = Icons.Default.SettingsBrightness,
                            isSelected = themePreference == "system",
                            onClick = { onSetThemePreference("system") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 5. App Updates & Download History Section
        item {
            Text(
                text = "Library & Updates",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = TextPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileNavigationRow(
                        title = "App Updates",
                        icon = Icons.Default.SystemUpdate,
                        trailingBadge = if (isCheckingUpdates) "Checking..." else "Up to Date",
                        onClick = {
                            isCheckingUpdates = true
                            onShowMessage("Checking for updates... All store applications are up to date.")
                        }
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "App Download History",
                        icon = Icons.Default.History,
                        trailingBadge = if (installedCount > 0) "$installedCount Installed" else null,
                        onClick = onPurchasedClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Saved Apps & Wishlist",
                        icon = Icons.Default.FavoriteBorder,
                        trailingBadge = if (wishlistCount > 0) "$wishlistCount" else null,
                        onClick = onWishlistClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Ratings & Reviews",
                        icon = Icons.Default.RateReview,
                        onClick = { onShowMessage("Your published store reviews and ratings") }
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Purchases & Subscriptions",
                        icon = Icons.Default.DateRange,
                        trailingBadge = if (purchasedCount > 0) "$purchasedCount" else null,
                        onClick = onSubscriptionsClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Redeem Gift Card or Code",
                        icon = Icons.Default.CardGiftcard,
                        onClick = onRedeemGiftCardClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 6. Settings, Help & Support, About Section
        item {
            Text(
                text = "Support & About",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = TextPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileNavigationRow(
                        title = "Store Notifications",
                        icon = Icons.Default.NotificationsNone,
                        trailingBadge = if (unreadNotificationsCount > 0) "$unreadNotificationsCount" else null,
                        onClick = onNotificationsClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Help & Support",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = onHelpClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "About Spoiled Store",
                        icon = Icons.Default.Info,
                        trailingBadge = "v2.4.0",
                        onClick = onAboutClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // 7. SIGN OUT BUTTON - AT THE VERY BOTTOM OF THE PROFILE SECTION
        item {
            if (userProfile.isSignedIn) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignOutClick() }
                        .testTag("profile_signout_bottom_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignInClick() }
                        .testTag("profile_signin_bottom_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign In to Spoiled Store",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else SurfaceSecondary,
        animationSpec = tween(200),
        label = "ThemeBgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(200),
        label = "ThemeContentColor"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (isSelected) Color.Black else BorderLight),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("theme_chip_${label.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp
                ),
                color = contentColor
            )
        }
    }
}
