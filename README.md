# Jim's Tick & Mosquito - Operations Manual Android App

Offline Android companion for the full **Operations, Growth & Expansion Manual (v3.0, September 15, 2026)**.

## Main screens
- **Home** - fast links, reading progress, key operating numbers.
- **Sections** - all 19 full manual sections.
- **Search** - full-text search across sections, glossary, acronyms and grants.
- **Quick** - operating thresholds and field checklists.
- **More** - glossary, acronyms, grants, favorites, source hierarchy, backup/print, exact reference PDFs.

## Offline behavior
All manual text, glossary content, grants and the exact reference PDFs are bundled in the APK. Internet is not required to read or search the manual.

## Local data
The app stores only reading progress, bookmarks, checklist state and optional personal notes in WebView local storage. It does **not** replace GorillaDesk and does not store customer payment/contracts/application records.

## Build target
- Samsung SM-S948U1 / Android 16 / One UI 8.5 baseline
- compile/target SDK 36
- min SDK 29
- Java 17
- Android Gradle Plugin 8.13.2 / Gradle 8.13
- no third-party Android libraries

## Build
Open in Android Studio with SDK 36 installed and build `app-debug.apk`, or push the project to GitHub and run the included **Build Android APK** workflow.
