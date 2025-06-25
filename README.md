# ShowList
ShowList is a simple Android application for creating and managing a personal list of TV shows. Built with Java, it uses a local SQLite database to store and manage show data, providing a basic, offline-first experience for tracking your favorite series.

## Features

*   **View Your Shows:** Displays a scrollable list of your saved TV shows, each with a title and rating.
*   **Detailed View:** Tap any show in the list to navigate to a dedicated screen with a larger image and a full description.
*   **Add New Shows:** Easily add new entries to your list using a floating action button that opens a form for the title, rating, and description.
*   **Delete Shows:** Remove shows from the list by long-pressing an item and selecting the 'Delete' option from the context menu.
*   **Local Storage:** All show data is stored locally on your device using an SQLite database, which is pre-populated with a few examples.

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

*   Android Studio
*   An Android device or emulator

### Installation

1.  Clone the repository:
    ```sh
    git clone https://github.com/crypticwaffles/showlist.git
    ```
2.  Open the project directory in Android Studio.
3.  Allow Gradle to sync the project dependencies.
4.  Build and run the application on your Android emulator or physical device.

## Project Structure

The project follows a standard Android application structure. Key components are located in the `app/src/main/` directory.

*   **`java/com/prog/showlist/`**: Contains the core Java source code.
    *   `ShowCategoryActivity.java`: The main activity that serves as the entry point, displaying the list of all shows.
    *   `ShowActivity.java`: The activity for displaying the detailed information of a single selected show.
    *   `AddShowActivity.java`: The activity that provides the form for adding a new show to the database.
    *   `DatabaseHelper.java`: Manages all SQLite database operations, including table creation, versioning, and data insertion/deletion.
    *   `ShowAdapter.java`: A custom `ArrayAdapter` to populate the `ListView` in `ShowCategoryActivity` with show data.
    *   `Show.java`: The data model (POJO) that represents a single TV show.

*   **`res/`**: Contains all non-code resources.
    *   `layout/`: XML files that define the user interface for each activity (`activity_show_category.xml`, `activity_show.xml`, etc.).
    *   `drawable/`: Image assets and vector drawables used in the application.
    *   `menu/`: Defines the context menu for delete operations.
    *   `values/`: Resource files for strings, colors, and themes.
