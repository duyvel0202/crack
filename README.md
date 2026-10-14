# DiscordRPC Android

## Build

Requires JDK 17 and Gradle 8.2. Run `gradle assembleDebug` from this directory. GitHub Actions builds the debug APK and publishes it as an artifact.

## Important

The supplied implementation attempts to authenticate to Discord Gateway using a user token. Automating a normal user account this way is unsupported by Discord and may result in account restrictions. Do not share or commit a Discord token. The app's custom-presence connection is not guaranteed to work with Discord's current gateway policies.
