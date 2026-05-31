# MyFriendz Feature Roadmap

## Purpose

This roadmap translates the system requirements into implementation phases. It separates current app behavior from near-term product commitments and future platform expansion.

## Current baseline

The current app is a local-first Android contact tracker with friend CRUD and basic last-contacted data. It does not yet implement scheduled reminders, Android notifications, or persisted interaction history.

## Phase 0 — Stabilize implementation foundation

Status: already approved separately in the maintenance board.

Goals:

- Configure the host/JDK/Gradle verification path.
- Add state/error coverage for add/edit/detail ViewModels.
- Align dispatcher injection across coroutine ViewModels.

Why this comes first:

- Reminder/history work will touch ViewModels, domain use cases, Room, and background workers.
- A stable test/dispatcher foundation reduces regression risk.

## Phase 1 — Requirements and documentation baseline

Status: this documentation task.

Deliverables:

- `docs/system-requirements.md`
- `docs/feature-roadmap.md`
- Existing vs future feature inventory.
- System requirements suitable for implementation planning and acceptance checks.

Acceptance criteria:

- Existing features are identified.
- Required reminder, notification, history, and external communication features are documented.
- Future in-app chat/calling is documented as roadmap work, not implied current behavior.

## Phase 2 — Reminder domain model and due/upcoming UI

Goal:

Create the local data and domain foundation for reminders before background scheduling.

Candidate implementation tasks:

1. Replace or wrap raw `Friend.frequency: String` with a validated reminder cadence model.
2. Add due-date calculation in the domain layer.
3. Add tests for due-date calculations:
   - due today
   - overdue
   - not due yet
   - weekly cadence
   - monthly cadence
   - custom-day cadence
4. Show due/upcoming status in the friend list and detail screen.
5. Keep this phase free of Android notification behavior.

Acceptance criteria:

- The app can compute and display a friend’s next contact due date.
- Invalid cadence values are handled deterministically.
- Due/upcoming state is tested without Android framework dependencies.

## Phase 3 — Interaction history

Goal:

Persist and display contacted history.

Candidate implementation tasks:

1. Add `ContactInteraction` or `InteractionHistory` Room entity.
2. Add DAO queries for:
   - insert interaction
   - list interactions for friend ordered newest-first
   - delete/cascade handling for friend deletion
3. Add repository and use-case methods.
4. Update “mark contacted” behavior so it creates a history event and updates `Friend.lastContacted`.
5. Add UI for logging an interaction with channel and optional notes.
6. Add UI for viewing a friend’s history timeline.

Acceptance criteria:

- Interactions persist across app restarts.
- Each friend detail screen can show interaction history.
- Marking as contacted updates both history and last-contacted date.
- Tests cover success and deletion/missing-friend behavior.

## Phase 4 — Android scheduled reminders and system notifications

Goal:

Deliver Android system notifications for due friends.

Candidate implementation tasks:

1. Add WorkManager dependency and scheduler abstraction.
2. Create a due-reminder worker that queries Room for due friends.
3. Create notification channel setup.
4. Handle Android 13+ notification permission flow.
5. Create reminder notifications with deep links to friend details.
6. Prevent duplicate notification spam for the same due friend/window.
7. Add manual verification documentation for Android background behavior.

Acceptance criteria:

- A due friend can trigger an Android system notification.
- Notification tap opens the correct friend or app screen.
- Missing notification permission is handled gracefully.
- Duplicate notification behavior is controlled.
- Tests cover pure due-selection logic; manual verification covers OS notification delivery.

## Phase 5 — External app communication actions

Goal:

Let users initiate communication from MyFriendz through apps already installed on the device.

Candidate implementation tasks:

1. Add communication action model:
   - phone
   - SMS/text
   - email
   - WhatsApp
   - other compatible external apps later
2. Add domain/presentation logic to expose only available actions based on friend data.
3. Add Android intent handlers for:
   - dialer/phone
   - SMS
   - email
   - WhatsApp/generic messaging
4. Add graceful fallbacks when data is missing or an app is unavailable.
5. Prompt user to log an interaction after launching an external communication action.

Acceptance criteria:

- Phone/SMS/email actions launch appropriate external apps when data exists.
- WhatsApp action launches when supported and fails gracefully when unavailable.
- The app does not automatically claim contact occurred just because an external app was opened.
- User can log the resulting interaction.

## Phase 6 — Future in-app communication infrastructure

Goal:

Prepare for in-app chat/calling when product, backend, account, and privacy requirements exist.

Future requirements to define before implementation:

- Identity and sign-in model.
- Friend/account matching.
- Backend service and storage architecture.
- Push notification architecture.
- Message encryption/privacy policy.
- Abuse/moderation/reporting model.
- Audio/video calling provider or protocol.
- Offline and sync behavior.

Acceptance criteria for starting this phase:

- Backend architecture is selected.
- Security/privacy requirements are approved.
- In-app communication is explicitly split from local reminder/history functionality.

## Recommended implementation order

1. Phase 0: foundation cleanup.
2. Phase 1: requirements/docs baseline.
3. Phase 2: local reminder calculation and due UI.
4. Phase 3: interaction history.
5. Phase 4: scheduled Android notifications.
6. Phase 5: external app communication actions.
7. Phase 6: future in-app communication.

## Notes

- Android system notifications and external app intents should be implemented before in-app chat/calling.
- The interaction-history model should be designed now to support future channels like `IN_APP_CHAT` and `IN_APP_CALL` without requiring a second history system.
- WorkManager is the preferred reminder scheduling baseline unless exact-time reminders become a hard product requirement.
