# MyFriendz

**MyFriendz** is an Android application designed to help you stay connected with your friends by sending timely reminders to reach out. In our fast-paced lives, it's easy to lose touch; MyFriendz ensures that doesn't happen.

## Current Features

* 📇 **Contact Management**: Add, edit, delete, and view friends you want to keep in touch with.
* 🗓️ **Last Contacted Tracking**: Store when a friend was last contacted and update that date when marking them as contacted.
* 📱 **Contact Details**: Store phone, email, comments, and a reminder-frequency value for each friend.
* 🎨 **User-Friendly Interface**: Clean Android UI built with Fragments, Navigation, Data Binding, Room, and Hilt.

## Roadmap

The product roadmap expands MyFriendz from a contact tracker into a relationship reminder app:

* ⏰ **Scheduled Reminders**: Calculate due/upcoming friends from each friend’s contact cadence.
* 🔔 **Android System Notifications**: Use Android-supported background scheduling and notification channels to remind the user when friends are due for contact.
* 📊 **Interaction History**: Persist and display a chronological history of contacted events.
* 💬 **External Communication Actions**: Launch external apps such as Phone, SMS, email, WhatsApp, or another compatible Android app from friend detail/reminder flows.
* 🚀 **Future In-App Communication**: Leave room for in-app chat/calling once backend, identity, privacy, and media infrastructure are designed.

See [`docs/system-requirements.md`](docs/system-requirements.md) and [`docs/feature-roadmap.md`](docs/feature-roadmap.md) for the detailed requirements and phased delivery plan. Local verification notes live in [`docs/development-notes.md`](docs/development-notes.md).

## Getting Started

### Prerequisites

* Android Studio Meerkat | 2024.3.1 Patch 1 or newer
* Android device or emulator running Android 8.0 (API level 26) or higher

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/e-Garcia/myfriendz.git
   ```

2. **Open in Android Studio:**

    * Launch Android Studio.
    * Click on `File` > `Open` and navigate to the cloned `myfriendz` directory.

3. **Build and Run:**

    * Click on the `Run` button or press `Shift + F10` to build and launch the app on your device or emulator.

## Usage

1. **Add Friends:**

    * Tap the "+" button to add a new contact.
    * Enter the friend’s name and contact details.
    * Set or record the friend’s contact cadence/last-contacted information where supported by the current UI.

2. **Manage Friends:**

    * Use the home screen to view saved friends.
    * Tap a friend to view details, edit information, delete the friend, or mark the friend as contacted.

3. **Planned Reminder Workflow:**

    * Future versions will show due/upcoming reminders, schedule background reminder checks, send Android system notifications, and record a contacted history timeline.

## Project Structure

* `app/`: Contains the main application code.

    * `src/main/java/`: Java/Kotlin source files.
    * `src/main/res/`: Resource files (layouts, strings, etc.).
* `gradle/`: Gradle wrapper files.
* `build.gradle`: Project-level Gradle configuration.
* `settings.gradle`: Settings for project modules.

## Architecture

The project follows a **clean architecture pattern** to separate concerns and enhance maintainability. The architecture enforces clear boundaries between layers and ensures the domain layer remains platform-agnostic.

### Architecture Layers

```
|------------------------|
|    Presentation        | → ViewModels, Fragments, UI (Data Binding)
|------------------------|
         ↓ (depends on)
|------------------------|
|      Domain            | → UseCases, Repository Interfaces
|------------------------|
         ↓ (implements)
|------------------------|
|       Data             | → Repository Implementations, Room DAOs
|------------------------|
         ↓ (uses)
|------------------------|
|  Frameworks / APIs     | → Room, Hilt (external dependencies)
|------------------------|
```

### Key Architectural Components
#### Presentation Layer (`app/src/main/java/.../view`, `viewmodel`)
- **ViewModels**: Manage UI state and business logic coordination
  - Use **StateFlow** for reactive state management
  - Inject `FriendUseCase` via Hilt dependency injection
  - Platform-agnostic (no Android context dependencies)
- **Fragments**: Handle UI rendering and user interactions
  - Use Data Binding for declarative UI
  - Observe StateFlow from ViewModels using `lifecycleScope.launch`
- **Example**: `FriendsListViewModel` injects `FriendUseCase` and calls its methods

#### Domain Layer (`app/src/main/java/.../domain`)
- **Use Case**: `FriendUseCase` - Unified class for all friend operations
  - Platform-agnostic business logic
  - Single point of interaction between presentation and data layers
  - Methods:
    - `getAllFriends()`: Retrieve all friends
    - `getFriend(id)`: Retrieve a specific friend
    - `addFriend(friend)`: Add a new friend
    - `updateFriendAsContacted(friend)`: Mark friend as contacted (updates lastContacted)
    - `updateFriendDetails(friend)`: Edit friend details (preserves lastContacted)
    - `deleteFriend(id)`: Remove a friend
- **Repository Interface**: `FriendRepository` - Defines contracts for data access
  - Abstract data source operations

#### Data Layer (`app/src/main/java/.../data`, `model`)
- **Repository Implementations**: Concrete data access implementations
  - `FriendRepositoryImpl`: Implements `FriendRepository` using Room DAO
- **DAOs**: Room database access objects
  - `FriendDao`: CRUD operations for Friend entity
- **Entities**: Room database entities
  - `Friend`: Core data model with Room annotations

#### Dependency Injection (Hilt)
- **Production Modules**:
  - `DatabaseModule`: Provides Room database and DAOs
  - `RepositoryModule`: Binds `FriendRepository` interface to `FriendRepositoryImpl`
- **Test Modules**:
  - `TestRepositoryModule`: Provides a mock repository for Hilt-based tests (primarily for integration/DI tests).
    - Note: For pure unit tests, prefer manually mocking `FriendRepository`/`FriendUseCase` or using lightweight test doubles instead of relying on test Hilt modules that require DI wiring.
  - Uses `@TestInstallIn` to replace production modules during Hilt tests

### Data Flow Example

```
User Action (Fragment)
    ↓
ViewModel.updateFriend()
    ↓
FriendUseCase.updateFriendAsContacted(friend)
    ↓
FriendRepository.updateFriend()
    ↓
FriendRepositoryImpl → FriendDao
    ↓
Room Database
    ↓
StateFlow.emit(Success)
    ↓
Fragment observes state
    ↓
UI Update
```

### Testing Strategy

- **ViewModels**: Test with mocked `FriendUseCase` using MockK
- **Use Case**: Test with mocked `FriendRepository` using MockK
- **Repository**: Test with mocked DAO or in-memory database
- **Unit tests**: Use `kotlinx-coroutines-test` with `StandardTestDispatcher` or `UnconfinedTestDispatcher`
- **Test isolation**: Hilt test modules provide mocked dependencies automatically


## Contributing

Contributions are welcome! Here's how you can help:

* **Bug Reports**: Submit issues for any bugs or glitches you encounter.
* **Feature Requests**: Suggest new features or enhancements.
* **Pull Requests**: Fork the repository and submit pull requests for improvements.

Please ensure your code adheres to the existing style and includes appropriate tests.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Additional Resources

* [Figma Design Prototype](https://www.figma.com/file/TCkJ5TMveQKM8UOFgrYxD0/Friendz-app?node-id=0%3A1) for the app's UI/UX design.
