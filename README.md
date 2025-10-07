#  TaskMaster Android App

##  Overview
**TaskMaster** is a modern Android task management application designed to help users efficiently manage their daily to-do items. The app integrates **Google Single Sign-On (SSO)**, a **Firebase backend**, and connects to a **REST API** for data storage and retrieval. TaskMaster allows users to add, edit, and delete tasks, while also providing a customizable **settings menu** and an intuitive, visually consistent **user interface**.

---

##  Purpose of the App
The primary purpose of TaskMaster is to:
- Provide users with a **simple and secure platform** to manage their tasks.
- Demonstrate the integration of **Firebase Authentication**, **REST APIs**, and **Kotlin-based Android UI development**.
- Implement **Single Sign-On (SSO)** using **Google Sign-In** for secure and seamless login.
- Showcase connectivity between a **mobile client** and a **hosted backend database** through RESTful services.
- Offer configurable **user settings** to personalize the app experience.

---

##  Design Considerations

###  Architecture
TaskMaster follows the **MVVM (Model-View-ViewModel)** architecture pattern to ensure modularity, scalability, and easy maintenance. The design separates logic, data handling, and UI representation.

###  UI/UX Design
- Clean and minimal interface using **Material Design principles**.
- Soft color palette with accessible contrast levels.
- Icons and typography maintain consistency across all screens.
- Simple navigation between **Login**, **Main**, and **Settings** screens.
- Responsive layouts optimized for both small and large Android devices.

###  Performance & Usability
- Uses **Firebase Authentication** for fast and secure login.
- Tasks are fetched and synced with the REST API to ensure persistence.
- Data is cached locally for smoother user experience during offline usage.
- Settings allow users to modify preferences like theme, sorting order, and notifications.

---

##  Key Features

### 1. **Single Sign-On (SSO) with Google**
- Seamless sign-in using the user’s existing Google account.
- Secure Firebase authentication for user validation.
- Auto-login enabled for returning users.

### 2. **Task Management**
- Add, edit, delete, and mark tasks as complete.
- Automatically sorted based on date or user preference.
- REST API integration for remote data storage.

### 3. **Settings Menu**
- User-configurable options for:
  - Theme (light/dark)
  - Auto-sort toggle
  - Notifications
- Stores user preferences locally using **SharedPreferences**.

### 4. **REST API Integration**
- Tasks are sent and retrieved through a **custom REST API**.
- The API is hosted online on **Render.com** and connects to a **remote MongoDB database**.
- Implemented using standard **CRUD operations (Create, Read, Update, Delete)**.

---

##  REST API Overview

| Endpoint | Method | Description |
|-----------|--------|-------------|
| `/tasks` | GET | Retrieve all tasks |
| `/tasks/:id` | GET | Retrieve a specific task |
| `/tasks` | POST | Add a new task |
| `/tasks/:id` | PUT | Update a task |
| `/tasks/:id` | DELETE | Delete a task |

The app communicates with this API using **Retrofit** in Kotlin for type-safe HTTP requests.

---

##  GitHub Usage

TaskMaster is developed collaboratively using **GitHub** for version control.

### 🧩 Git Workflow
- Each developer works on feature branches (`feature/settings`, `feature/api`, etc.).
- Pull requests are used for merging into the `main` branch.
- Issues and commits are properly tagged with descriptive messages.
- Frequent commits show consistent progress and code history.

###  Repository Includes
- Complete Android Studio project files.
- `README.md` file (this document).
- `.gitignore` file configured for Android projects.
- Workflow YAML for **GitHub Actions** CI/CD.

---

##  GitHub Actions & Continuous Integration

GitHub Actions are used to automate testing and build verification for the project.

###  Example Workflow (`.github/workflows/android.yml`)
```yaml
name: Android CI

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: "17"
          distribution: "temurin"

      - name: Build with Gradle
        run: ./gradlew build
```

This ensures that every commit and pull request triggers an automated build process for error detection and continuous delivery validation.

---

##  Technologies Used

| Category | Technology |
|-----------|-------------|
| Language | Kotlin |
| IDE | Android Studio |
| Authentication | Firebase Authentication (Google SSO) |
| Backend | Render.com, Custom Rest API |
| Networking | Retrofit |
| Database | Firebase / Remote DB |
| UI Framework | Material Design |
| Hosting | GitHub + Firebase Hosting (optional) |
| Version Control | Git + GitHub |
| CI/CD | GitHub Actions |

---

##  Installation Guide

1. **Clone the repository**
   ```bash
   git clone https://github.com/VCDN-2025/prog7314-poe-part-2-ST10300672.git
   ```

2. **Open the project**
   - Open in **Android Studio**.
   - Sync Gradle files.

3. **Set up Firebase**
   - Add your `google-services.json` file inside `app/`.
   - Enable Google Sign-In in Firebase Console.

4. **Run the app**
   - Select your emulator or device.
   - Click **Run ▶️** in Android Studio.

---

##  Demonstration Video
A professional demonstration video has been prepared showing:
- Google Sign-In (SSO)
- Task creation and deletion
- Settings menu in action
- REST API communication
- Overall UI and usability

Youtube Link: (https://youtu.be/43vzrM2iIHQ)



---

## 👥 Authors
Developed by **Brayden Pillay**  
ST10300672 


---

## 🏁 Conclusion
TaskMaster demonstrates a complete Android development cycle — from concept to deployment — showcasing key skills in mobile app development, cloud integration, and version-controlled collaboration.
---

