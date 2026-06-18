# 🍪 Cookie Browser — Native Android

A fully native Android browser with a built-in cookie grabber.  
**No Capacitor. No WebView localhost. No iframe.**

## Features
- Real Android WebView (loads any site directly)
- Address bar with Google search fallback
- Back / Forward / Reload / Home buttons
- Progress bar while loading
- 🍪 Get Cookies button → shows all cookies for current site
- Copy cookies as plain text or JSON
- Tap individual cookie to copy name=value
- Dark theme

## Build via GitHub Actions (no PC needed)

1. Push this repo to GitHub
2. Go to **Actions** tab → **Build Native APK**
3. Click **Run workflow**
4. When done, download the APK from **Artifacts**
5. Install on your Android phone

## Build locally (if you have Android Studio)
```
./gradlew assembleDebug
```
APK will be at: `app/build/outputs/apk/debug/app-debug.apk`
