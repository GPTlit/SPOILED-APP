# Spoiled Store - Cloud Database Schema (PostgreSQL / Supabase)

This SQL schema powers the multi-platform Spoiled Store marketplace, synchronizing the Android mobile application, the public web portal, and the administrator dashboard.

```sql
-- 1. PROFILES & ROLES
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    username TEXT,
    display_name TEXT,
    avatar_url TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Admin email policy trigger
CREATE OR REPLACE FUNCTION handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, username, display_name, is_admin)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1)),
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1)),
        LOWER(NEW.email) IN ('salemmoustapha15@gmail.com', 'salemmoustapha19@gmail.com')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. APPLICATIONS & GAMES
CREATE TABLE IF NOT EXISTS apps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    developer TEXT NOT NULL,
    bundle_id TEXT NOT NULL,
    version TEXT NOT NULL DEFAULT '1.0.0',
    tagline TEXT,
    description TEXT,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    size_formatted TEXT NOT NULL DEFAULT '0 MB',
    icon_url TEXT,
    apk_file_url TEXT NOT NULL,
    rating NUMERIC(2, 1) DEFAULT 5.0,
    reviews_count INT DEFAULT 0,
    downloads_count INT DEFAULT 0,
    price NUMERIC(10, 2) DEFAULT 0.0,
    is_subscription BOOLEAN DEFAULT FALSE,
    is_top_app BOOLEAN DEFAULT FALSE,
    is_top_game BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT TRUE,
    overview TEXT,
    speed_score TEXT DEFAULT '99/100',
    security_status TEXT DEFAULT 'Verified Sandboxed',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. APP SCREENSHOTS
CREATE TABLE IF NOT EXISTS app_screenshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES apps(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    display_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. RATINGS & COMMENTS
CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES apps(id) ON DELETE CASCADE,
    user_id UUID REFERENCES profiles(id) ON DELETE SET NULL,
    user_name TEXT NOT NULL,
    user_email TEXT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_app_review UNIQUE(app_id, user_email)
);

-- 5. NEWS & EDITORIALS
CREATE TABLE IF NOT EXISTS news_articles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    category TEXT NOT NULL DEFAULT 'Store News',
    summary TEXT NOT NULL,
    content TEXT NOT NULL,
    author TEXT NOT NULL DEFAULT 'Admin',
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. BROADCAST NOTIFICATIONS
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_broadcast BOOLEAN DEFAULT TRUE,
    target_user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. ROW LEVEL SECURITY (RLS)
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE apps ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_screenshots ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews ENABLE ROW LEVEL SECURITY;
ALTER TABLE news_articles ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- Public can read published catalog
CREATE POLICY "Public can view published apps" ON apps FOR SELECT USING (is_published = true);
CREATE POLICY "Public can view screenshots" ON app_screenshots FOR SELECT USING (true);
CREATE POLICY "Public can view reviews" ON reviews FOR SELECT USING (true);
CREATE POLICY "Public can view news" ON news_articles FOR SELECT USING (true);
CREATE POLICY "Public can view broadcast notifications" ON notifications FOR SELECT USING (is_broadcast = true);

-- Authenticated users can post reviews
CREATE POLICY "Users can create reviews" ON reviews FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- Only Admin can insert/update/delete apps, news, and notifications
CREATE POLICY "Admin full access apps" ON apps FOR ALL USING (
    LOWER(auth.jwt() ->> 'email') IN ('salemmoustapha15@gmail.com', 'salemmoustapha19@gmail.com')
);
CREATE POLICY "Admin full access news" ON news_articles FOR ALL USING (
    LOWER(auth.jwt() ->> 'email') IN ('salemmoustapha15@gmail.com', 'salemmoustapha19@gmail.com')
);
CREATE POLICY "Admin full access notifications" ON notifications FOR ALL USING (
    LOWER(auth.jwt() ->> 'email') IN ('salemmoustapha15@gmail.com', 'salemmoustapha19@gmail.com')
);

-- Storage Buckets Configuration:
-- Bucket 1: app-apks (public read for downloads, restricted upload for admins)
-- Bucket 2: app-icons (public read, restricted upload for admins)
-- Bucket 3: app-screenshots (public read, restricted upload for admins)
```
