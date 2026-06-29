# MyFriendz Development Notes

## Purpose

This document captures local development and verification notes that are useful to maintainers but should not clutter the public README.

## Local verification

The project is an Android/Kotlin app that targets Java 17 bytecode. Local command-line verification requires a Java runtime/JDK available to the shell.

Recommended command once Java/JDK is configured:

```bash
bash ./gradlew testReleaseUnitTest
```

Use Android Studio for normal IDE builds and emulator/device runs.

## NextCloud-mounted workspace note

When the repository is checked out under the user’s NextCloud-mounted project folder, the mount can make `gradlew` executable-bit metadata appear modified or prevent direct `./gradlew` execution.

If direct wrapper execution fails, run Gradle through Bash instead:

```bash
bash ./gradlew testReleaseUnitTest
```

If Git status is polluted only by a mode-only `gradlew` difference, treat that as local environment noise rather than an application-code change. Do not commit `gradlew` mode-only changes unless intentionally fixing wrapper metadata outside the NextCloud mount.

## Current implementation baseline

As of the requirements baseline in `docs/system-requirements.md`, the app currently focuses on local friend CRUD/details flows. Scheduled reminder delivery, Android system notifications, and persisted interaction history are roadmap features, not completed implementation.
