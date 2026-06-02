# DoggySitter

DoggySitter is an Android MVP that connects dog owners with dog walkers. Owners can publish
walk requests for their dogs, walkers can accept available requests in their service area, and
administrators can inspect system data.

## Main Features

- Email and password registration and login with role selection
- Owner dog management and walk request creation
- Walker profile, availability, service area, request acceptance, and job completion
- Owner request cancellation and completed-walk reviews
- Admin lists and aggregate statistics for users, walkers, requests, and reviews

## Tech Stack

- Android app written in Java
- Gradle Kotlin DSL build files
- Firebase Authentication
- Cloud Firestore
- Firebase Storage rules
- Google Play Services Location

## Project Structure

- `app/src/main/java/com/example/doggysitter/activities`: Android screens grouped by role
- `app/src/main/java/com/example/doggysitter/repositories`: Firebase access and transaction logic
- `app/src/main/java/com/example/doggysitter/models`: Firestore-backed data models
- `app/src/main/java/com/example/doggysitter/adapters`: RecyclerView adapters
- `app/src/main/java/com/example/doggysitter/utils`: validation, formatting, location, and error helpers
- `firestore.rules`: Cloud Firestore security rules
- `storage.rules`: Firebase Storage security rules

## Build JDK Requirement

This project uses Android Gradle Plugin 8.5 and requires JDK 17 or newer to run Gradle.
The app module still targets Java 8 bytecode for Android compatibility; that setting does not
change the JDK required by Gradle.

In Android Studio, open the Gradle settings and select the Embedded JDK / JBR 17+ as the Gradle
JDK. From a terminal, set `JAVA_HOME` to a JDK 17+ installation before running the wrapper:

```powershell
$env:JAVA_HOME = "C:\path\to\jdk-17-or-newer"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat --version
```

## Run the Project

1. Open the repository in Android Studio.
2. Set the Gradle JDK to the Embedded JDK / JBR 17+.
3. Add the Firebase Android configuration file at `app/google-services.json`.
4. Select an Android device or emulator with API 26 or newer.
5. Build and run the `app` configuration.

From a terminal on Windows, a debug APK can be built with:

```powershell
.\gradlew.bat assembleDebug
```

## Firebase Setup

The app requires a Firebase project with Email/Password Authentication and Cloud Firestore enabled.
The project-specific `app/google-services.json` file is required. Deploy `firestore.rules` and
`storage.rules` to the Firebase project before using the app outside local development.
