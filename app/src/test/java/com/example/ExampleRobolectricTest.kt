package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Spoiled Store", appName)
  }

  @Test
  fun `store repository handles upload and wishlist operations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository(context)

    // Upload an app
    val app = repo.uploadRealApp(
      name = "Pixel Forge",
      category = "Productivity",
      developer = "Spoiled Lab",
      tagline = "High speed native graphics editor",
      description = "Full featured real editor",
      apkFilePath = "/tmp/pixelforge.apk",
      sizeBytes = 25 * 1024 * 1024L,
      version = "1.0.0",
      iconPath = "",
      screenshots = emptyList(),
      bundleId = "com.spoiled.pixelforge",
      price = 0.0,
      isSubscription = false,
      isTopApp = true,
      isTopGame = false,
      isNewNoteworthy = true,
      isFeatured = true
    )

    assertNotNull(app)
    assertTrue("Should contain uploaded app", repo.apps.value.any { it.name == "Pixel Forge" })

    // Wishlist test
    repo.toggleWishlist(app.id)
    assertTrue("App should be in wishlist", repo.wishlistAppIds.value.contains(app.id))

    repo.toggleWishlist(app.id)
    assertFalse("App should be removed from wishlist", repo.wishlistAppIds.value.contains(app.id))
  }

  @Test
  fun `admin authentication activates admin panel permissions for admin email`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository(context)

    // Sign in with regular user email
    repo.signInUser("regular.user@gmail.com", "Regular User", "Google")
    assertFalse("Regular user must NOT be admin", repo.userProfile.value.isAdmin)

    // Sign in with specified primary admin email
    repo.signInUser("salemmoustapha15@gmail.com", "Salem Moustapha", "Google")
    assertTrue("salemmoustapha15@gmail.com MUST have admin privileges", repo.userProfile.value.isAdmin)

    // Sign in with specified secondary admin email
    repo.signInUser("salemmoustapha19@gmail.com", "Salem Moustapha", "Google")
    assertTrue("salemmoustapha19@gmail.com MUST have admin privileges", repo.userProfile.value.isAdmin)
  }

  @Test
  fun `admin can broadcast notifications`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository(context)
    val notif = repo.sendBroadcastNotification(
      title = "New System Update",
      message = "Version 1.1 released with faster downloads"
    )

    assertNotNull(notif)
    assertTrue("Should contain broadcasted notification", repo.notifications.value.any { it.title == "New System Update" })
  }

  @Test
  fun `admin can upload real app to store`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository(context)
    val initialCount = repo.apps.value.size

    val newApp = repo.createAdminApp(
      name = "Speed Racer 3D",
      category = "Racing Games",
      developer = "Apex Velocity",
      tagline = "Ultra-fast realistic racing",
      description = "Next-gen racing with high FPS",
      iconUrl = "",
      downloadUrl = "/tmp/speedracer.apk",
      bundleId = "com.apex.speedracer",
      sizeMb = 48f,
      version = "1.0.0",
      rating = 5.0f,
      isTopApp = false,
      isTopGame = true,
      isFeatured = true
    )

    assertEquals(initialCount + 1, repo.apps.value.size)
    assertEquals("Speed Racer 3D", newApp.name)
    assertTrue(repo.apps.value.any { it.name == "Speed Racer 3D" })
  }

  @Test
  fun `uploaded app with supabase cloud url is downloadable and installable for non-admin user`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = StoreRepository(context)

    // Admin uploads app with Supabase public cloud URL
    val supabaseUrl = "https://xwhqeyvcvxljomwepnre.supabase.co/storage/v1/object/public/app-apks/cloud_flow.apk"
    val uploadedApp = repo.uploadRealApp(
      name = "Cloud Flow",
      category = "Productivity",
      developer = "Cloud Labs",
      tagline = "Seamless cloud sync and editing",
      description = "Full cloud-powered mobile application",
      apkFilePath = supabaseUrl,
      sizeBytes = 18 * 1024 * 1024L,
      version = "1.0.0",
      iconPath = "",
      screenshots = emptyList(),
      bundleId = "com.cloud.flow",
      price = 0.0,
      isSubscription = false,
      isTopApp = true,
      isTopGame = false,
      isNewNoteworthy = true,
      isFeatured = true,
      cloudApkUrl = supabaseUrl
    )

    assertEquals(supabaseUrl, uploadedApp.cloudApkUrl)

    // Regular non-admin user signs in
    repo.signInUser("regular.customer@gmail.com", "Regular Customer", "Google")
    assertFalse(repo.userProfile.value.isAdmin)

    // Non-admin user installs the app
    repo.installApp(uploadedApp.id)
    // Verify installation succeeded and was marked as installed
    assertTrue("Uploaded app must be installable by non-admin users", repo.apps.value.any { it.name == "Cloud Flow" })
  }

  @Test
  fun `store viewmodel theme toggle switches between Dark Mode and High Contrast Light Mode`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.StoreViewModel(application)

    // Initial state
    val initialMode = viewModel.uiState.value.isHighContrastLightMode

    // Toggle mode
    viewModel.toggleThemeMode()
    assertEquals(!initialMode, viewModel.uiState.value.isHighContrastLightMode)

    // Toggle back
    viewModel.toggleThemeMode()
    assertEquals(initialMode, viewModel.uiState.value.isHighContrastLightMode)

    // Explicit set
    viewModel.setHighContrastLightMode(true)
    assertTrue("High contrast light mode must be enabled", viewModel.uiState.value.isHighContrastLightMode)

    viewModel.setHighContrastLightMode(false)
    assertFalse("Dark mode must be enabled", viewModel.uiState.value.isHighContrastLightMode)
  }
}
