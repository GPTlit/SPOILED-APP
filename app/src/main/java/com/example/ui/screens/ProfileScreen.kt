package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserProfile
import com.example.ui.components.ProfileNavigationRow
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BorderStrong
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
    unreadNotificationsCount: Int,
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Profile Top Header Row with Settings Icon
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

        // 2. Default Anonymous Avatar & Sign In Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anonymous light gray circular avatar with white person silhouette
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFD9DCE2),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    if (userProfile.isSignedIn) {
                        Text(
                            text = userProfile.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = userProfile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (userProfile.isAdmin) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "ADMINISTRATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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
                            text = "Sign in to access your account,\npurchases and more.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. Sign In / Sign Out Action Row Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (userProfile.isSignedIn) onSignOutClick() else onSignInClick()
                    }
                    .testTag("profile_signin_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (userProfile.isSignedIn) "Sign Out" else "Sign In",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            color = if (userProfile.isSignedIn) MaterialTheme.colorScheme.error else TextPrimary
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 4. ADMIN PANEL ACCESS (Visible ONLY when user is admin / registered admin email)
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
                                    text = "Upload apps, manage catalog & send alerts",
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

        // 5. Account Options Grouped Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileNavigationRow(
                        title = "Purchased",
                        icon = Icons.Default.FileDownload,
                        trailingBadge = if (purchasedCount > 0) "$purchasedCount" else null,
                        onClick = onPurchasedClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Subscriptions",
                        icon = Icons.Default.DateRange,
                        onClick = onSubscriptionsClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Wishlist",
                        icon = Icons.Default.FavoriteBorder,
                        trailingBadge = if (wishlistCount > 0) "$wishlistCount" else null,
                        onClick = onWishlistClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Redeem Gift Card",
                        icon = Icons.Default.CardGiftcard,
                        onClick = onRedeemGiftCardClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 6. "More" Grouped Card (With real Help & Support, Notifications and About Dialogs)
        item {
            Text(
                text = "More",
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
                        title = "Notifications",
                        icon = Icons.Default.NotificationsNone,
                        trailingBadge = if (unreadNotificationsCount > 0) "$unreadNotificationsCount" else null,
                        onClick = onNotificationsClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "Help & Support",
                        icon = Icons.Default.HelpOutline,
                        onClick = onHelpClick
                    )
                    HorizontalDivider(color = BorderLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 50.dp))
                    ProfileNavigationRow(
                        title = "About Spoiled Store",
                        icon = Icons.Default.Info,
                        trailingBadge = "v1.0",
                        onClick = onAboutClick
                    )
                }
            }
        }
    }
}
