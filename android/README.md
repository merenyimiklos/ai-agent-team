# UgorjBe Android client

The debug app connects to the host API from an Android Emulator at
`http://10.0.2.2:8080/`. Retrofit endpoint paths own the `/api` prefix.

Requirements: JDK 17 and Android SDK 36 (Build Tools 36.0.0). From this directory:

```powershell
./gradlew.bat testDebugUnitTest assembleDebug
```

Start the root Docker Compose stack first, then sign in with the demo credentials
documented in the root README. The debug manifest permits cleartext traffic solely
for local emulator development; release builds do not.
