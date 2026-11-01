# Android release signing

The public APK must be signed with one persistent Android signing key. GitHub-hosted runners are ephemeral, so their automatically generated debug keystores must never be used for distributable update APKs.

## Create the signing key once

Run on a trusted computer with JDK 17 or newer:

```powershell
keytool -genkeypair -v -keystore iie-release.jks -alias iie-release -keyalg RSA -keysize 4096 -validity 10000
```

Keep the keystore and both passwords in a secure backup. Do not commit the keystore or passwords to this repository. Losing the key prevents signing compatible updates.

## Configure GitHub Actions secrets

In the repository, open **Settings → Secrets and variables → Actions → New repository secret** and create:

- `IIE_KEYSTORE_BASE64`: Base64 encoding of the complete `iie-release.jks` file. In PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes(".\iie-release.jks"))`.
- `IIE_KEYSTORE_PASSWORD`: keystore password.
- `IIE_KEY_ALIAS`: `iie-release` (or the alias selected during key generation).
- `IIE_KEY_PASSWORD`: key password.

The release workflow fails closed when any secret is missing. It decodes the keystore only into the temporary GitHub Actions runner, builds the `release` variant, verifies its APK signature, and publishes the APK and SHA-256 file.

## Update compatibility

Every future APK must use this same keystore and alias. Never generate a replacement key for an existing installation. The Android application ID must also remain `com.iie.android`, and each release must increase `versionCode`.

The first stable-signing release cannot update installations signed with a different debug certificate. Those installations must be uninstalled once before installing the new signed APK; subsequent releases can update in place.
