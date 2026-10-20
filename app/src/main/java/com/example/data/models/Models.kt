package com.example.data.models

data class AppItem(
    val id: String,
    val slug: String,
    val name: String,
    val category: String,
    val developer: String,
    val rating: Float = 5.0f,
    val reviewsCount: Int = 0,
    val downloadsCount: Int = 0,
    val sizeBytes: Long = 0L,
    val sizeFormatted: String = "0 MB",
    val version: String = "1.0.0",
    val description: String = "",
    val tagline: String = "",
    val iconPath: String = "",
    val apkFilePath: String = "",
    val screenshots: List<String> = emptyList(),
    val bundleId: String = "",
    val price: Double = 0.0,
    val isSubscription: Boolean = false,
    val isTopApp: Boolean = false,
    val isTopGame: Boolean = false,
    val isNewNoteworthy: Boolean = false,
    val isFeatured: Boolean = false,
    val isUploadedByAdmin: Boolean = true,
    val releaseDate: String = "Today",
    val changelog: String = "Initial release.",
    val overview: String = "High performance application verified for Spoiled Store ecosystem.",
    val speedScore: String = "99/100",
    val securityStatus: String = "Verified & Sandboxed",
    val cloudApkUrl: String = "",
    val downloadSource: String = "Cloud / Storage"
) {
    // Convenient compatibility accessors
    val iconUrl: String get() = iconPath
    val iconFallbackText: String get() = name.take(2).uppercase().ifBlank { "SS" }
    val iconColorHex: Long get() = 0xFF1E293B
    val sizeMb: Float get() = if (sizeBytes > 0) sizeBytes / (1024f * 1024f) else 15f
}

data class CommentItem(
    val id: String,
    val appId: String,
    val userName: String,
    val userEmail: String,
    val comment: String,
    val rating: Int = 5,
    val date: String = "Just now"
)

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val author: String,
    val date: String,
    val category: String,
    val readTime: String,
    val imagePath: String = ""
)

data class StoreNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isBroadcast: Boolean = true,
    val isRead: Boolean = false
)

data class UserProfile(
    val isSignedIn: Boolean = false,
    val email: String = "",
    val username: String = "",
    val authProvider: String = "Email",
    val isAdmin: Boolean = false
) {
    val displayName: String get() = username.ifBlank { email.substringBefore("@").ifBlank { "User" } }
}

enum class StoreNavigationTab(val label: String) {
    HOME("Homepage"),
    APPS("Apps"),
    GAMES("Games"),
    NEWS("News"),
    PROFILE("Profile")
}

object StoreCategories {
    val ALL_CATEGORIES = listOf(
        "Productivity",
        "Utilities",
        "Developer Tools",
        "Social Networking",
        "Entertainment",
        "Photo & Video",
        "Music & Audio",
        "Finance & Payments",
        "Education",
        "Health & Fitness",
        "Lifestyle & Food",
        "News & Magazines",
        "Books & Reference",
        "Business",
        "Graphics & Design",
        "Navigation & Travel",
        "Weather",
        "Shopping",
        "Action Games",
        "Adventure Games",
        "Arcade Games",
        "Board & Puzzle",
        "Card Games",
        "Casino Games",
        "Casual Games",
        "Racing Games",
        "Role-Playing (RPG)",
        "Simulation Games",
        "Sports Games",
        "Strategy Games",
        "Trivia & Word",
        "Offline Games",
        "Online Multiplayer"
    )

    val APP_CATEGORIES = listOf(
        "All",
        "Productivity",
        "Utilities",
        "Developer Tools",
        "Social Networking",
        "Entertainment",
        "Photo & Video",
        "Music & Audio",
        "Finance & Payments",
        "Education",
        "Health & Fitness",
        "Lifestyle & Food",
        "News & Magazines",
        "Books & Reference",
        "Business",
        "Graphics & Design",
        "Navigation & Travel",
        "Weather",
        "Shopping"
    )

    val GAME_GENRES = listOf(
        "All",
        "Action Games",
        "Adventure Games",
        "Arcade Games",
        "Board & Puzzle",
        "Card Games",
        "Casino Games",
        "Casual Games",
        "Racing Games",
        "Role-Playing (RPG)",
        "Simulation Games",
        "Sports Games",
        "Strategy Games",
        "Trivia & Word",
        "Offline Games",
        "Online Multiplayer"
    )
}
