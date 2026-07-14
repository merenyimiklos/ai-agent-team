# UgorjBe Android client

The native Kotlin/Compose client opens a map-first Explore experience backed by
the real API. Map and List share one bounded result envelope, filters and marker
selection. The debug app connects from an Android Emulator to
`http://10.0.2.2:8081/`; Retrofit endpoint paths own the `/api` prefix.

## Requirements

- JDK 17
- Android SDK 36 and Build Tools 36.0.0
- an emulator/device with Google Play services for the map
- the root API running on host port 8081

## Local Maps key

Create or update the gitignored `android/local.properties` file:

```properties
sdk.dir=C\:\\Users\\you\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=your_local_restricted_key
```

In Google Cloud, enable only **Maps SDK for Android** for this key. Apply the
Android application restriction for package `hu.ugorjbe.app` and the SHA-1 of
the signing certificate used locally. The debug SHA-1 is available from:

```powershell
./gradlew.bat signingReport
```

Do not commit the key. `secrets.defaults.properties` contains only
`DEFAULT_API_KEY`, which lets CI compile. A placeholder/missing key shows a
recoverable map-unavailable panel and the live-backend List action; it never
switches to mock data. Approximate location is optional, is requested only from
the in-context locate action, and denial leaves Budapest discovery usable.

## Build and test

From `android/`:

```powershell
./gradlew.bat testDebugUnitTest
./gradlew.bat lintDebug assembleDebug assembleDebugAndroidTest
./gradlew.bat connectedDebugAndroidTest # with a running emulator/device
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Start the root
development stack first, then sign in with the demo customer credentials from
the root README. Debug permits cleartext only for the emulator-local API;
release builds do not.
