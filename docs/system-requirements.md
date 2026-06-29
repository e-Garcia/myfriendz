# MyFriendz System Requirements

## Document control

- Project: `myfriendz`
- Repository path: `/home/egarcia/NextCloud/Documents/Projects/Code/myfriendz`
- Status: Draft requirements baseline
- Last updated: 2026-05-31
- Source of truth: Product direction provided by Erick Garcia plus current source-tree inspection

## 1. Purpose

MyFriendz helps users maintain relationships by tracking friends, remembering when they were last contacted, scheduling future contact reminders, showing Android system notifications when follow-up is due, and preserving a history of interactions.

The current app already supports friend management and stores friend metadata. This document distinguishes existing behavior from required future behavior so implementation work can be planned, tested, and accepted like an industry software project.

## 2. Scope

### 2.1 In scope

- Friend/contact management.
- Reminder cadence per friend.
- Scheduled reminders and Android system notifications.
- Contacted/interactions history.
- External communication handoff through installed apps such as Phone, SMS, email, WhatsApp, or other supported Android intent handlers.
- Future in-app communication infrastructure for chat/calling when backend/media infrastructure exists.
- Requirements, roadmap, and implementation planning documentation under `docs/`.

### 2.2 Out of scope for the first reminder/history implementation

- A custom backend service.
- Real-time in-app chat.
- In-app voice/video calling.
- Cross-device sync.
- Social graph import.
- AI message drafting or relationship scoring.

These are future roadmap capabilities and should not block the first local-first Android implementation.

## 3. Stakeholders and user roles

- Primary user: a person who wants to remember to contact friends on a recurring cadence.
- Friend/contact: the person being tracked inside the app; not an app user unless future sharing/sync features are added.
- Android OS: owns notification permission, notification delivery, background scheduling constraints, and external-app intent routing.
- External communication apps: Phone, SMS, email client, WhatsApp, and other apps capable of handling Android communication intents.

## 4. Existing system inventory

Current source-tree inspection shows the app has:

- Android app module using Kotlin, Room, Hilt, Navigation, and Data Binding.
- `Friend` Room entity with fields:
  - `name`
  - `lastContacted`
  - `frequency` as `String`
  - `phone`
  - `email`
  - `comments`
  - generated `uuid`
- `FriendDao` CRUD operations.
- `FriendRepository` / `FriendRepositoryImpl` data access boundary.
- `FriendUseCase` with friend operations, including `updateFriendAsContacted(friend)` which updates `lastContacted` to `LocalDate.now()`.
- List, add, edit, and detail screens.

Current scan did not find:

- A notification/reminder scheduler.
- Android notification channel setup.
- Runtime notification permission handling.
- WorkManager or AlarmManager integration.
- An interaction-history Room entity/table.
- UI for viewing a chronological interaction timeline.
- UI or domain layer for launching communication actions.

## 5. Product capability map

### 5.1 Existing capabilities

- FR-EX-001: The system shall allow the user to create friend records.
- FR-EX-002: The system shall allow the user to edit friend records.
- FR-EX-003: The system shall allow the user to delete friend records.
- FR-EX-004: The system shall display a list of friends.
- FR-EX-005: The system shall display friend details.
- FR-EX-006: The system shall store a friend's last-contacted date.
- FR-EX-007: The system shall store friend phone, email, comments, and a reminder-frequency value.
- FR-EX-008: The system shall support marking a friend as contacted by updating `lastContacted`.

### 5.2 Required near-term capabilities

- FR-REM-001: The system shall allow the user to configure a reminder cadence for each friend.
- FR-REM-002: The system shall calculate the next due date for each friend from `lastContacted` and the configured cadence.
- FR-REM-003: The system shall identify friends who are due or overdue for contact.
- FR-REM-004: The system shall schedule background checks for due reminders using an Android-supported scheduling mechanism.
- FR-REM-005: The system shall show Android system notifications for friends who are due for contact.
- FR-REM-006: The system shall provide notification actions or deep links that open the relevant friend detail screen.
- FR-REM-007: The system shall handle Android notification runtime permission requirements.
- FR-REM-008: The system shall avoid duplicate reminder notifications for the same friend and due window.
- FR-REM-009: The system shall allow a user to mark a reminder as contacted, snoozed, or dismissed if these actions are supported in the UI/notification flow.

### 5.3 Required interaction-history capabilities

- FR-HIST-001: The system shall persist contact/interactions as history records linked to a friend.
- FR-HIST-002: Each interaction record shall include at minimum friend ID, timestamp/date, contact channel, and optional notes.
- FR-HIST-003: The system shall allow the user to log an interaction manually.
- FR-HIST-004: Marking a friend as contacted shall create an interaction-history record.
- FR-HIST-005: The system shall display a friend’s interaction history in reverse chronological order.
- FR-HIST-006: The system shall update the friend’s `lastContacted` value from the most recent contact interaction.
- FR-HIST-007: Deleting a friend shall deterministically handle associated interaction-history records, either by cascade delete or by a documented retention policy.

### 5.4 Required external communication capabilities

- FR-COMM-001: The system shall allow the user to initiate a phone call through an external app when a friend has a phone number.
- FR-COMM-002: The system shall allow the user to initiate an SMS/text message through an external app when a friend has a phone number.
- FR-COMM-003: The system shall allow the user to initiate an email through an external app when a friend has an email address.
- FR-COMM-004: The system shall allow the user to open WhatsApp or another compatible messaging app through Android intents when supported by installed apps and available contact data.
- FR-COMM-005: The system shall show clear disabled states or errors when a requested external communication channel is unavailable.
- FR-COMM-006: The system shall optionally prompt the user to log an interaction after returning from an external communication action.

### 5.5 Future in-app communication capabilities

- FR-FUT-001: The architecture shall leave room for future in-app chat.
- FR-FUT-002: The architecture shall leave room for future in-app audio/video calling.
- FR-FUT-003: Future in-app communication shall be treated as a separate capability area requiring backend, identity, privacy, security, and moderation requirements.
- FR-FUT-004: Future in-app communication events shall integrate with the same interaction-history model instead of creating a parallel history system.

## 6. Data requirements

### 6.1 Friend

The existing `Friend` entity should be evolved carefully. Future implementation should consider replacing the raw `frequency: String` with a validated, migration-safe representation.

Required friend-related data:

- Friend ID.
- Display name.
- Phone number.
- Email address.
- Comments/notes.
- Last-contacted date.
- Reminder cadence.
- Reminder enabled/disabled state.
- Optional reminder snooze state.

### 6.2 Reminder cadence

The reminder cadence should support at least:

- Weekly.
- Monthly.
- Custom number of days.

Implementation may store cadence as:

- enum + optional day count, or
- integer days + display label.

Acceptance requirement: invalid cadence values must not silently break reminder calculation.

### 6.3 Interaction history

Proposed entity: `InteractionHistory` or `ContactInteraction`.

Minimum fields:

- `id`: generated primary key.
- `friendId`: foreign key to `Friend`.
- `contactedAt`: timestamp or local date/time.
- `channel`: enum/string such as `PHONE`, `SMS`, `EMAIL`, `WHATSAPP`, `IN_PERSON`, `OTHER`, future `IN_APP_CHAT`, future `IN_APP_CALL`.
- `notes`: optional text.
- `createdAt`: record creation timestamp if different from `contactedAt`.

Recommended constraints:

- Foreign-key relation to `Friend`.
- Index on `friendId`.
- Sort by `contactedAt DESC` for timeline display.

## 7. Scheduling and notification requirements

### 7.1 Scheduling

- SR-SCHED-001: The system shall use an Android-supported scheduling mechanism for due-reminder checks.
- SR-SCHED-002: WorkManager is the preferred default for periodic, battery-aware reminder checks.
- SR-SCHED-003: Exact alarms should be avoided unless product requirements later demand exact time reminders and Android permission tradeoffs are accepted.
- SR-SCHED-004: The scheduler shall be resilient across app restarts and device reboots within platform constraints.
- SR-SCHED-005: The scheduler shall read reminder state from Room instead of relying only on in-memory state.

### 7.2 Notifications

- SR-NOTIF-001: The system shall create a notification channel for reminder notifications on Android versions that require channels.
- SR-NOTIF-002: The system shall request or guide the user through notification permission requirements on Android 13+.
- SR-NOTIF-003: Reminder notifications shall include the friend name and an action/deep link to the friend details screen.
- SR-NOTIF-004: Notification content shall avoid exposing sensitive notes unless explicitly designed and approved.
- SR-NOTIF-005: Notifications shall not repeatedly spam the same due friend without a state transition such as contacted, snoozed, next due date, or next daily digest window.

## 8. External app integration requirements

- SR-EXT-001: External communication should use Android intents rather than direct third-party SDKs for the first implementation.
- SR-EXT-002: Phone calls should use the safest available intent flow first, such as opening the dialer with the number rather than requiring direct-call permission.
- SR-EXT-003: SMS should open the default SMS app with the destination number and optional draft text if supported.
- SR-EXT-004: Email should open an email intent with the destination address.
- SR-EXT-005: WhatsApp should be launched through a package-aware or generic intent when available, with graceful fallback when not installed.
- SR-EXT-006: The app shall not claim that communication happened just because an external intent was launched; history should be logged by explicit user confirmation or post-action prompt.

## 9. Non-functional requirements

- NFR-001 Maintainability: Feature work shall preserve the clean architecture separation between presentation, domain, and data layers.
- NFR-002 Testability: Reminder date calculations and interaction-history use cases shall be unit tested without Android framework dependencies.
- NFR-003 Reliability: Due-reminder calculation shall be deterministic for boundary cases such as today, overdue, leap days, and cadence changes.
- NFR-004 Privacy: Contact data and interaction history shall remain local unless a future sync feature is explicitly designed and approved.
- NFR-005 Battery awareness: Background checks shall respect Android background execution limits and avoid excessive wakeups.
- NFR-006 Accessibility: Reminder/history UI should use clear labels and support standard Android accessibility behavior.
- NFR-007 Compatibility: The app currently targets Android API 34 and supports minSdk 26; implementation should respect platform behavior across this range.
- NFR-008 Migration safety: Room schema changes shall include migrations or a documented development-only fallback if destructive migration is intentionally accepted.

## 10. Acceptance criteria summary

The reminder/history feature set is accepted when:

- A friend can have a valid reminder cadence.
- The app can compute and display when that friend is due.
- Background scheduling checks due reminders.
- Android system notifications appear for due friends after permission/channel setup.
- Notification taps route to the correct friend or relevant app screen.
- The user can log contact interactions.
- Contact history is persisted and visible per friend.
- Marking a friend as contacted updates both `lastContacted` and interaction history.
- External communication actions can launch appropriate apps and gracefully handle missing data/apps.
- Documentation under `docs/` clearly separates existing, planned, and future capabilities.

## 11. Open questions

- Should reminders be exact-time reminders or approximate daily checks?
- Should notifications be per-friend or a daily digest?
- Should snooze be part of the first notification implementation?
- Should interaction history be date-only or date-time?
- Should external WhatsApp support require explicit user-provided WhatsApp numbers, or reuse phone numbers?
- Should future in-app communication require accounts/sign-in, or remain out of scope until a backend is selected?
