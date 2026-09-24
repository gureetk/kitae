# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Plans are made of named training days (Push, Pull, Legs, Upper, …) instead of weekdays. Pick a day on the home screen and start it whenever you train
- Sets are planned ahead for every exercise of a day. Starting a day loads its sets into the workout, where each set is ticked off with a button once done
- Rest timer between sets: starts when a set is ticked off, keeps counting with the screen off, shows a countdown notification with +15s and Skip, and alerts when the rest is over. Its length can be changed (or turned off) in settings
- Weights can be shown and entered in kilograms or pounds
- Workouts can be finished, keeping the sets that were not done as skipped (shown dimmed in the history, never counted as done) or removing them. The plan itself is never changed
- A check mark in the plan editor to finish editing a plan
- A workout stays in progress until it is finished. Starting a day again after finishing always begins fresh from the plan

### Changed

- Existing plans are converted: every weekday becomes a day named after it, with sets planned from the last time each exercise was performed. Past sessions are linked to these days
- Statistics, history and suggestions only count sets that were done
- Sessions show the name of their day, and more than one workout can be logged on the same date
- Sets of an ongoing workout can be edited by tapping them
- Picking a plan to follow brings you back to the home screen

### Removed

- Plan templates without any exercises, only Push Pull Leg is included. Unused ones are removed when updating

### Fixed

- Removing an exercise from a plan no longer removes it from every other plan
- Past sessions are read-only, swiping a set away in them could crash the app

### Added
- Last performed set data already inserted

### Changed
- Redesign set adding UI
- Activity graph design

## [1.3.3] - 2026-04-11

### Added
- Backup and restore for whole data
- Activity graph on the Profile screen
- Support for 35 additional locales
- Plan Edit button in session detail

### Changed
- Removed Rest timer (will be replaced soon)
- Updated Plan Edit page design
- Better animations

## [1.3.2] - 2025-11-13

### Added
- Session History card on Home screen
- Set Type selection for sets
- Timer which shows time since last set
- Monochrome launcher icon on Android 12+
- Allow adding a new exercise directly when it cannot be found in the list
- Clean up empty plans from the Plans screen via confirmation dialog
- Enable predictive back navigation
- Empty state on Sessions screen

### Changed
- Removed Bottom navigation bar, added user icon to top bar
- Updated Day Switcher component styling for clarity
- Hide Lifts card when there are no lifts
- Redesigned Session History card and screen
- Replaced profile icon on Home screen
- Removed "Today" label on Sessions and filtered out empty sessions

### Fixed
- Select Plan button alignment
- Prevent unintended translation for Turkish app name
- Session list now shows most recent first
- Lifts card not showing even when lifts existed
- Sets from past sessions not shown when the exercise was removed from the corresponding plan

## [1.3.0] - 2025-01-17

### Added
- Drag text field in "Add Set"
- Double tap to edit "set info"
- History Icon (You can check last week's session if it exists)
- Support for Monochrome icon on Android 12+
- Text animation on Onboarding
- Safer way to delete Sets / Exercises / Plans
- New Font for headings

### Changed
- Targets Android 15
- Onboarding screen
- Default theme for new users
- Sorting of muscle groups chips
- Always save plan on going back
- Color in Profile
- Home Screen and On-boarding Screen
- Some buttons and UI elements

### Fixed
- Save button not visible
- Two `Default` theme in Settings
- Scrolling on `Select Exercise` Sheet
- Performance issues on `Add Set` Sheet
- Weird line in the setting wave
- Crash on deleting plan
- On boarding not completing
- Loads of performance improvements

### Removed
- Gradient in settings

## [1.2.0] - 2024-05-26

### Added
- Support for isometric exercises
- Deleting Sets / Exercises / Plans

### Changed
- Error message height
- Chips type in `Select Exercise`

### Fixed
- Navigation to same page again
- Double back presses
- Swipe gesture on reps and weight text field
- Elements squashing on small screens
- Empty exercises
- Invalid reference
- False reference icon

## [1.1.1] - 2024-05-19

### Fixed
- Navigation from home screen
- Annoying animations on home page
- Plan Edit Page
- Back button on all pages

## [1.1.0] - 2024-05-19

### Added
- New Home Page
- Back button on Exercises Page
- Option to open References from workout page(if added)

### Changed
- Splash Screen Image to reduce dependency on `NonFreeNet`
- Whole Plan card is clickable

### Fixed
- APK dependency tree encryption
- Color of icons on some buttons
- `Zestful` Color Palettes
- Crash when using invalid reference
- UI/UX for Exercises Page
- Some navigation crashes

## [1.0.0] - 2024-05-12

### Added
- Initial Release
