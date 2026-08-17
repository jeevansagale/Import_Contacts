# Contact Importer

A premium Android application that transforms contact importing into an immersive, character-themed experience.

Unlike traditional contact import utilities, Contact Importer combines a modern Material 3 interface, dynamic theming, high-performance animations, and an extensible character library to create a polished experience while maintaining reliable contact import functionality.

---

## Overview

Contact Importer is built around a modular architecture that separates data, theming, and UI into independent components. Characters are defined entirely through metadata and assets, allowing new themes to be added without modifying application logic.

The project demonstrates modern Android development practices using Jetpack Compose, Material 3, MVVM architecture, and a data-driven design.

---

## Features

### Dynamic Character Library

- Metadata-driven character system
- Runtime character discovery
- Registry-based loading
- Series categorization
- Asset validation
- Extensible architecture

### Character Themes

Every character includes:

- Hero artwork
- Portrait artwork
- Background artwork
- Custom color palette
- Typography configuration
- Animation profile
- Theme metadata

Current supported franchises include:

- Genshin Impact
- Zenless Zone Zero
- Chainsaw Man
- Bleach
- Attack on Titan

---

## Contact Import

- JSON-based contact importing
- Validation before import
- Duplicate handling
- Error reporting
- Import verification
- Android Contacts Provider integration

---

## User Interface

Designed using modern Android design principles.

Highlights include:

- Material 3
- Dynamic color system
- Animated transitions
- Character-aware backgrounds
- Responsive layouts
- High-performance image loading
- Theme-aware components

---

## Architecture

```
app
├── data
│   ├── Character Registry
│   ├── Metadata Loader
│   ├── Contact Parser
│   ├── Theme Repository
│   └── Models
│
├── theme
│   ├── Theme Engine
│   ├── Theme Tokens
│   └── Theme Presets
│
├── ui
│   ├── Components
│   ├── Animations
│   ├── Backgrounds
│   └── Image Helpers
│
└── Main
    ├── Activity
    └── ViewModel
```

The application follows the MVVM pattern with a clean separation between business logic, presentation, and data layers.

---

## Character System

Characters are stored externally under the `Data/characters` directory.

Example:

```
Data/
└── characters/
    └── Genshin Impact/
        └── Furina/
            ├── metadata.json
            └── images/
                ├── hero.jpg
                ├── portrait.jpg
                └── background.jpg
```

Adding a new character requires only:

1. Creating a new folder.
2. Adding the required images.
3. Providing a valid `metadata.json`.
4. Updating the registry.

No application code changes are required.

---

## Technology Stack

### Language

- Kotlin

### UI

- Jetpack Compose
- Material 3

### Architecture

- MVVM
- Repository Pattern
- StateFlow

### Android APIs

- Contacts Provider
- Runtime Permissions
- Android Resources

### Build System

- Gradle Kotlin DSL

---

## Project Structure

```
Data/
    characters/
        registry.json

app/
    data/
    theme/
    ui/
    MainActivity.kt
    MainViewModel.kt
```

---

## Testing

The project includes:

- Unit tests
- Contact parser tests
- Theme validation tests
- Android instrumentation tests
- Import verification tests

---

## Design Goals

The project focuses on:

- Maintainability
- Scalability
- Extensibility
- Performance
- Separation of concerns
- Modern Android architecture
- Clean UI implementation

---

## Performance

The application is designed to:

- Load character metadata dynamically
- Prevent malformed metadata from crashing the application
- Minimize unnecessary recomposition
- Reduce runtime allocations
- Support future expansion without architectural changes

---

## Future Roadmap

Planned improvements include:

- Additional character libraries
- Search and filtering
- Theme customization
- Cloud synchronization
- Asset packs
- Import history
- Backup and restore
- Localization support

---

## Requirements

- Android Studio
- Android SDK
- Gradle
- Android device running Android 8.0 (API 26) or later

---

## Building

Clone the repository:

```bash
git clone https://github.com/<username>/Contact_Importer.git
```

Open the project in Android Studio.

Build using Gradle:

```bash
./gradlew assembleDebug
```

Or use Android Studio's build system.

---

## License

This project is intended for educational and portfolio purposes.

Character names, artwork, and related intellectual property belong to their respective owners.
Only the application implementation, architecture, and source code are part of this repository.

---

## Acknowledgements

Built using the Android Jetpack ecosystem and modern Android development best practices.

Special thanks to the developers and maintainers of Kotlin, Jetpack Compose, Material 3, and the Android Open Source Project for the tools and frameworks that made this project possible.
