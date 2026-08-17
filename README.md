# CONTACT IMPORTER

**A lightweight, customizable Android contact importer built for speed, reliability, and a polished user experience.**

Import contacts from JSON files or pasted JSON directly into your Android Contacts.

---

## Features

* **JSON Import** — File picker or paste JSON directly.
* **Flexible Formats** — Supports root arrays and `contacts`, `data`, and `items` wrappers.
* **Flexible Fields** — Accepts both `number` and `phone`.
* **Flexible Types** — Phone values can be JSON strings or numbers.
* **Duplicate Detection** — Detects duplicates using normalized phone numbers.
* **Fault-Tolerant Parsing** — Invalid records don't stop valid contacts from importing.
* **Batch Import** — Designed for large contact lists without freezing the UI.
* **Character Themes** — Customizable character-based UI and artwork.
* **Local Processing** — No account, backend, Firebase, or cloud processing required.

---

## JSON Support

### Recommended

```json
[
  {
    "name": "Furina",
    "number": "+919876543210"
  },
  {
    "name": "Raiden Shogun",
    "phone": "+919876543211"
  }
]
```

The following are also supported:

```text
Root Array
├── number / phone
├── String / Number values
│
Wrapped Object
├── contacts
├── data
└── items
```

Phone numbers are converted to **String internally** so values such as `+91` and leading zeros are preserved.

---

## Import Pipeline

```text
JSON
 ↓
Format Detection
 ↓
Parsing & Normalization
 ↓
Validation
 ↓
Duplicate Detection
 ↓
Conflict Resolution
 ↓
Batch Import
 ↓
Android Contacts
```

A malformed contact is skipped instead of causing the entire import to fail.

---

## UI / UX

The interface follows a **clean Material 3 design** with character theming layered on top.

Design priorities:

* Clear visual hierarchy
* Minimal clutter
* Responsive interactions
* Consistent spacing and typography
* Lightweight animations
* Accessible states and feedback
* Character artwork without sacrificing usability

The theme system is completely separated from the import engine, so changing characters cannot affect import reliability.

---

## Performance

CONTACT IMPORTER is optimized for both **performance and storage**, not just image size.

### Targets

| Metric         |                   Goal |
| -------------- | ---------------------: |
| APK            |     **25–35 MB ideal** |
| APK maximum    |             **<60 MB** |
| UI             |             **60 FPS** |
| Large imports  | **No ANR / UI freeze** |
| Target devices |         **2–4 GB RAM** |

Optimization focuses on:

* Lazy loading
* Efficient JSON parsing
* Batch contact operations
* Memory-conscious image loading
* R8 and resource shrinking
* Dependency minimization
* Low unnecessary background work
* Startup and rendering performance

---

## Tech Stack

* **Kotlin**
* **Jetpack Compose**
* **Material 3**
* **Kotlin Coroutines**
* **kotlinx.serialization**
* **Android Contacts Provider**
* **Storage Access Framework**

No backend is required for core functionality.

---

## Project Structure

```text
app/
└── src/main/
    ├── java/
    │   └── com/example/contactimporter/
    │       ├── data/
    │       ├── importer/
    │       ├── contacts/
    │       ├── theme/
    │       └── ui/
    │
    ├── assets/
    │   └── characters/
    │
    └── res/
```

---

## Design Philosophy

> **Fast enough for low-end devices.
> Flexible enough for real-world JSON.
> Polished enough to feel like a finished product.**

CONTACT IMPORTER aims to combine a lightweight Android architecture with a refined, customizable interface without letting visual features compromise performance or reliability.
