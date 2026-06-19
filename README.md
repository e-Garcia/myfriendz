# MyFriendz

**MyFriendz** is an Android application designed to help you stay connected with your friends by sending timely reminders to reach out. In our fast-paced lives, it's easy to lose touch; MyFriendz ensures that doesn't happen.

## Feature Baseline

This section separates what is currently implemented from the features that are still missing or incomplete. Use the missing-feature list as the starting point for issue/task breakdown.

### Current Implemented Features

* 📇 **Friend list**: Displays saved friends in a RecyclerView-backed home screen.
* ➕ **Add friend**: Adds a new friend from the add screen. Current add flow captures the friend's name and initializes `lastContacted` to six months ago by default.
* 👤 **Friend details**: Opens a detail screen for a selected friend and displays name, last-contacted date, reminder frequency text, phone, email, and comments when present.
* ✏️ **Edit friend details**: Edits name, email, phone, last-contacted date, and comments from the edit screen.
* 📅 **Last-contacted tracking**: Stores `lastContacted` dates and formats them as `dd/MM/yyyy`.
* ✅ **Mark as contacted**: Long-pressing a friend on the list updates `lastContacted` to today and refreshes the list.
* 🎨 **Contact status colors**: Colors list rows based on how long ago the friend was contacted:
  * Green: contacted within the last month.
  * Yellow: older than one month.
  * Orange: older than three months.
  * Red: older than six months.
* ☎️ **Phone action**: Tapping a populated phone value on the detail screen opens the Android dialer with the friend's number.
* 💾 **Local persistence**: Persists friends locally using Room with fields for name, last-contacted date, frequency, phone, email, and comments.
* 🧭 **Navigation**: Uses Android Navigation between list, details, add, and edit screens.
* 🧱 **Architecture**: Uses Clean Architecture-style layers with Fragments/XML/Data Binding, ViewModels, StateFlow/LiveData, Hilt dependency injection, Room, repositories, and a unified `FriendUseCase`.
* 🧪 **Testing foundation**: Includes unit-test dependencies and ViewModel/domain/repository testing patterns with MockK and coroutine test dispatchers.

### Missing or Incomplete Features to Break Down into Tasks

* ⏰ **Real reminder scheduling**: The app stores reminder-related data, but it does not schedule alarms, WorkManager jobs, or calendar/reminder events.
* 🔔 **Android notifications**: There is no notification channel, notification permission handling, or notification delivery when a friend is due.
* 📊 **Interaction history timeline**: The app updates a single `lastContacted` date, but it does not persist a history of each interaction.
* 📝 **Interaction logging UI**: There is no dedicated flow to record notes, interaction type, or outcome after contacting a friend.
* ⚙️ **Editable reminder frequency**: The model has a `frequency` field and the detail screen can display it, but the current add/edit forms do not let the user set or update frequency.
* 🧾 **Complete add-friend form**: The add flow only captures name; it should support phone, email, comments, reminder frequency, and initial last-contacted date.
* 🗑️ **Delete friend UI**: Repository/use-case/ViewModel delete support exists, but there is no confirmed user-facing delete action wired in the screens.
* 🔍 **Search, filtering, and sorting**: The friend list does not yet provide search, due/overdue filters, or ordering by urgency/name/last-contacted date.
* 📇 **Import from device contacts**: No current master-branch flow imports friends from Android Contacts.
* ✉️ **Email/message actions**: Detail screen displays email, but does not open email, SMS, or messaging intents.
* 🧪 **Instrumented and end-to-end tests**: Unit-test patterns exist, but user journeys such as add/edit/mark-contacted/delete need UI/instrumented coverage.
* 🎨 **Compose migration**: Project context says views should use Compose, but current screens are Fragments with XML layouts and Data Binding.
* ✅ **Input validation**: Add/edit screens need validation for required name, valid email/phone formats, and safe date/frequency values before saving.
* ♿ **Accessibility and empty states**: Current layouts need a complete pass for content descriptions, empty-list messaging, and touch target/accessibility behavior.
* 🔄 **Backup/sync support**: Friends are local-only in the current master-branch implementation; there is no cloud sync, export/import, or backup flow.

### Suggested Task Breakdown Order

1. **Requirements cleanup**: Convert each missing feature above into a GitHub issue with acceptance criteria.
2. **Add/edit form completeness**: Add missing friend fields to the add flow and frequency controls to add/edit.
3. **Delete friend flow**: Add a confirmed delete action and tests.
4. **Reminder domain model**: Define reminder frequency semantics and due-date calculation rules.
5. **Notification infrastructure**: Add notification permission, channel, scheduler, and delivery tests.
6. **Interaction history**: Add a Room entity and UI for historical contact logs.
7. **List productivity features**: Add sorting, filtering, and search.
8. **Contact actions/import**: Add contact import plus email/SMS/dial shortcuts.
9. **UI modernization**: Decide whether to migrate XML screens to Compose incrementally or keep XML for now.
10. **E2E coverage**: Add instrumented tests for the core flows after the product behavior is stable.

## Maintenance Status

_Last reviewed by the maintenance scout on 2026-06-02._

* Local verification: `bash ./gradlew testReleaseUnitTest` completed successfully in this checkout on 2026-06-02.
* README feature baseline updated on 2026-06-01 after inspecting the current master-branch source.
* Current UI/source structure uses Android Fragments, XML layouts, Navigation, and Data Binding under `app/src/main/java` and `app/src/main/res`.
* Reminder scheduling, Android notifications, and persisted interaction-history timeline work are tracked as proposed maintenance tasks until implemented and verified.
* Current checkout branch during the 2026-06-02 scout was `docs/myfriendz-readme-features`, with open PR #42 for the README feature-baseline documentation.

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

1. **Add friends:**

    * Tap the "+" button to add a new friend.
    * Enter the friend's name and save. Additional fields are currently added through the edit flow.

2. **Review friends:**

    * The home screen displays saved friends and their last-contacted dates.
    * Row colors indicate how stale the last-contacted date is.
    * Tap a friend to open details.

3. **Update contact status:**

    * Long-press a friend in the list to mark them as contacted today.

4. **Edit details:**

    * From the detail screen, tap edit to update name, email, phone, last-contacted date, and comments.
    * Tap a populated phone value in the detail screen to open the Android dialer.

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
