# Privify

Android app for encrypting sensitive files directly on the SD card (external storage) with a
user-chosen passphrase. No data is sent over the internet. Technically, file data is encrypted
using AES with a 256 bit key derived using PBKDF2, all using standard Android libraries.

Every file is individually salted and uses a separate Initialization Vector (IV). The passphrase is
kept in memory while the app is running and when closed only its hash is persistently stored.

The app is
[available in the Play Store](http://play.google.com/store/apps/details?id=se.joscarsson.privify),
but I encourage you to build from source yourself so you know what you actually run (even though I
promise it's the code you see in the repository that is in the APK).

## Develop

 1. Clone the repository.
 2. Open it in Android Studio 3.0.
 3. Do what you normally do :)

## Build

I haven't added any command-line make targets or such - so far I've only built the app through
Android Studio.

## FAQ

### Aren't there apps like this one already?

I looked before I built this app. My requirements was:

 * Possible to build from source (meaning open source).
 * Simple and intuitive, not relying on fixed-size virtual volumes (TrueCrypt, VeraCrypt).
 * No network activity (accounts, files synced to cloud providers, etc).

If you find a another app that fits those requirements, please let me know :)

### How can you guarantee the encryption provided by the app is really secure?

I don't guarantee anything, but the methods used are industry standard and the app uses only
Android-provided system libraries. As far as I know I follow all best practices for this kind
of encryption (as of Oct 2017), but feel free to review and point out mistakes/vulnerabilities.