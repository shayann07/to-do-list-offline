# To-Do List Offline

**To-Do List Offline** is an efficient, user-friendly Android application that allows you to manage your daily tasks in permanent offline mode. Built with modern Android development practices, the app ensures a seamless experience without requiring an internet connection.

## Features

- **Offline Functionality**: Operates entirely without internet, ensuring privacy and reliability.
- **Task Categorization**: Organize tasks into Morning, Afternoon, and Tonight categories.
- **Today’s Tasks Screen**: Displays tasks for the current day, categorized by time slots.
- **Dynamic UI**: RecyclerViews update dynamically based on available tasks, maintaining a clean interface.
- **Modern Tech Stack**: Built with Kotlin, MVVM architecture, and Room for efficient local storage.

## Tech Stack

- **Programming Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: Room
- **UI Components**: RecyclerView, LiveData

## Project Structure

```
app/
├── data/
│   ├── database/         # Room database setup
│   ├── model/            # Data models
│   └── repository/       # Data repositories
├── ui/
│   ├── fragments/        # UI fragments (Today, Add Task, etc.)
│   └── viewmodel/        # ViewModels for UI components
├── utils/                # Utility classes and functions
└── MainActivity.kt       # Host activity
```

## Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/shayann07/to-do-list-offline.git
   ```
2. **Open in Android Studio**: Import the project into Android Studio.
3. **Sync Gradle**: Ensure all dependencies are downloaded and synced.
4. **Run the App**: Build and run the app on an emulator or physical device.

## How It Works

1. **Task Storage**: Tasks are stored locally using the Room database.
2. **Task Filtering**: Tasks are filtered by date and time to populate the Today’s Tasks screen.
3. **Dynamic Visibility**: RecyclerViews in Morning, Afternoon, and Tonight categories are shown only if tasks exist for those slots.

## Contributing

Contributions are welcome! Follow these steps to contribute:

1. Fork the repository.
2. Create a new branch for your feature or bugfix.
3. Commit your changes with clear messages.
4. Submit a pull request with detailed descriptions.

## Acknowledgments

- Thanks to the Android community for providing resources and inspiration.
- Initial Firebase integration was removed to make the app fully offline-focused.

---

Feel free to explore and contribute to the project!
