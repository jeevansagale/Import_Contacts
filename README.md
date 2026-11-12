# Import Contacts

<p align="center">
  <strong>An Android utility for importing contacts from JSON files or pasted JSON data.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square" alt="Jetpack Compose" />
</p>

---

## Overview

Import Contacts reads contact records from a JSON file or pasted JSON text, validates the records, checks for existing phone numbers, and writes accepted contacts to the Android Contacts Provider.

The application is designed to handle imperfect input without abandoning the entire import when an individual record is invalid. Parsing and contact-provider operations are separated from the Compose UI.

## Screenshots

<p align="center">
  <img src="Data/Images_readme/image1.jpeg" width="23%" alt="Home screen" />
  <img src="Data/Images_readme/image2.jpeg" width="23%" alt="Import screen" />
  <img src="Data/Images_readme/image3.jpeg" width="23%" alt="Theme selection" />
  <img src="Data/Images_readme/image4.jpeg" width="23%" alt="Import result" />
</p>

## Features

- Import contacts from a JSON file using Android's document picker.
- Paste JSON directly into the application.
- Accept a root JSON array, a single contact object, or an object containing a `contacts`, `data`, or `items` array.
- Match `name`, `number`, and `phone` fields without case sensitivity.
- Validate names and phone numbers individually.
- Normalize common phone-number formatting characters before comparison.
- Detect existing contacts through Android's PhoneLookup provider.
- Insert and update contacts using batched `ContentProviderOperation` calls.
- Display import results and validation failures.
- Provide selectable character-based themes.

## Supported JSON

A root array:

```json
[
  {
    "name": "Ada Lovelace",
    "number": "+441234567890"
  },
  {
    "Name": "Alan Turing",
    "PHONE": "+441234567891"
  }
]
```

A wrapped array:

```json
{
  "contacts": [
    {
      "name": "Ada Lovelace",
      "phone": "+441234567890"
    }
  ]
}
```

The parser also accepts `data` and `items` as wrapper keys. Field and wrapper matching is case-insensitive.

## Import Pipeline

```text
JSON File / Pasted Text
          |
          v
      JSON Parsing
          |
          v
  Field and Type Validation
          |
          v
 Phone Number Normalization
          |
          v
 Existing Contact Lookup
          |
          v
 Insert / Update Operations
          |
          v
 Android Contacts Provider
```

Invalid records are reported individually; valid records remain available for import.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| JSON | kotlinx.serialization |
| Contact access | Android Contacts Provider |
| File input | Storage Access Framework |
| Background work | Kotlin coroutines |
| Build | Gradle, Android Gradle Plugin |

## Requirements

- Android Studio with Android SDK 36.
- JDK compatible with the Gradle wrapper.
- Android device or emulator running Android 10 (API 29) or later.
- Contacts permission when importing or updating device contacts.

The app module currently declares `minSdk 29`, `compileSdk 36`, and `targetSdk 36`.

## Build

```bash
git clone https://github.com/jeevansagale/Import_Contacts.git
cd Import_Contacts

# Run unit tests
./gradlew testDebugUnitTest

# Build a debug APK
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

## Tests

The repository includes unit tests for JSON parsing and character-theme behavior, plus instrumentation tests for import verification.

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

The instrumentation command requires a connected Android device or emulator.

## Permissions and Privacy

The application requests `READ_CONTACTS` and `WRITE_CONTACTS` to look up existing contacts and create or update records. Review the permission prompt before granting access.

The import flow is implemented using on-device Android APIs; this README does not make a claim about every third-party library or optional feature beyond the inspected import path.

## Repository Structure

```text
app/src/main/java/com/example/contactimporter/
├── data/       # JSON parsing, contact models, provider operations
├── theme/      # Theme engine and presets
├── ui/         # Compose UI and components
└── MainActivity.kt

Data/
├── Images_readme/  # README screenshots
└── characters/     # Theme artwork
```

## Third-Party Artwork

The repository contains character artwork associated with third-party entertainment properties. These assets remain subject to their respective owners' rights and license terms. Their presence does not imply endorsement or grant permission for reuse.

## License

No license file is currently included. Unless a license is added, the repository should be treated as source available for viewing, not as granting general permission to reuse or redistribute the code.
