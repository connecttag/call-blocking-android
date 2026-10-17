# Call Blocking Android

[![](https://jitpack.io/v/connecttag/call-blocking-android.svg)](https://jitpack.io/#connecttag/call-blocking-android)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A lightweight and robust Android library for call blocking management, phone number pattern matching, and Telecom `RoleManager` integration (`ROLE_CALL_SCREENING` & `ROLE_DIALER`).

## Features

- 🚫 **System BlockedNumberContract Repository**: Query, insert, and delete blocked numbers safely through `AndroidCallBlockingRepository`.
- 🔍 **Flexible Number Pattern Matching**: `BlockedNumberMatcher` supports exact match, E.164 normalization, and wildcard/prefix patterns (e.g. `+1800*`).
- 🛡️ **RoleManager Integration**: Helper class `CallBlockingRoleManager` to query and request Android 10+ (API 29+) `ROLE_CALL_SCREENING` or default dialer status.
- 💾 **CSV/Text Codec**: Import and export blocked number lists easily via `BlockedNumbersTextCodec`.

## Installation

Add JitPack repository to your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}
```

Add the dependency to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.connecttag:call-blocking-android:1.0.0")
}
```

## Quick Start

### 1. Query & Block Numbers

```kotlin
import org.connecttag.services.callblocking.AndroidCallBlockingRepository
import org.connecttag.services.callblocking.CallBlockingResult

val repository = AndroidCallBlockingRepository(context)

when (val result = repository.getBlockedNumbers()) {
    is CallBlockingResult.Success -> {
        println("Blocked numbers count: ${result.value.size}")
    }
    is CallBlockingResult.PermissionDenied -> println("Permission required")
    is CallBlockingResult.Unsupported -> println("Not supported on this device")
    is CallBlockingResult.Failure -> println("Error: ${result.throwable}")
}

// Add a blocked number
repository.addBlockedNumber("+1234567890")
```

### 2. Match Numbers Against Patterns

```kotlin
import org.connecttag.services.callblocking.BlockedNumberMatcher
import org.connecttag.services.callblocking.BlockedNumber

val isBlocked = BlockedNumberMatcher.matches(
    number = "+1234567890",
    blockedNumber = BlockedNumber(id = 1, number = "+1234*")
)
```

### 3. Check & Request Call Screening Role

```kotlin
import org.connecttag.services.callblocking.CallBlockingRoleManager

val roleManager = CallBlockingRoleManager(context)

if (!roleManager.isCallScreeningRoleHeld()) {
    val intent = roleManager.createCallScreeningRoleRequestIntent()
    intent?.let { context.startActivity(it) }
}
```

## License

```
Copyright 2026 Connect Tag

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
