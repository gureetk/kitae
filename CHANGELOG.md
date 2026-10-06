# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Kitae is a fork of [Kenko](https://github.com/Iamlooker/Kenko). Everything under Unreleased is new in Kitae,
the versions below it are Kenko's history.

## [Unreleased]

### Added

- A −15s button in the rest timer notification, next to +15s and Skip
- A clock showing how long the workout in progress has been going, and the length of each workout in the history

### Changed

- New app ID (`ca.gureet.kitae`) and signing key, so Kitae 1.x can't update to this version. To keep your data, use Backup Now under Backup & Restore in the old app's settings, then Restore in the new one. The old app can be uninstalled after that

### Fixed

- Reps changed just before tapping done weren't always saved, and a quick second tap could open the same set twice

## [1.1.0] - 2026-09-26

### Added

- 40 more exercises, including timed ones like planks and wall sits
- Finishing a workout asks whether sets you added should join the day

### Changed

- Warm-up, failure, drop and rest-pause sets are numbered on their own, as W1, F1, D1 and R1
- Warm-up sets stay together at the start of an exercise
- Buttons instead of a slider to change the weight of a set, in steps of 2.5 and 5
- Finishing a workout saves the reps, weights and set types you used to the plan

### Fixed

- The names of the Tricep Push Down and Calf Raises exercises

## [1.0.0] - 2026-09-24

### Added

- Plans are made of named training days (Push, Pull, Legs, Upper, …) instead of weekdays. Pick a day on the home screen and start it whenever you train
- Sets are planned ahead for every exercise of a day. Starting a day loads its sets into the workout, where each set is ticked off with a button once done
- Rest timer between sets: starts when a set is ticked off, keeps counting with the screen off, shows a countdown notification with +15s and Skip, and alerts when the rest is over. Its length can be changed (or turned off) in settings
- Weights can be shown and entered in kilograms or pounds
- Warm-up and failure sets. Warm-ups are marked with a W instead of a number and are left out of the performance rating, sets taken to failure count a little more
- Workouts can be finished keeping the sets that were not done as skipped (shown dimmed in the history, never counted as done, the plan stays the same) or removing them from the workout and from the day's plan. A workout where nothing was done can be discarded
- A check mark in the plan editor to finish editing a plan
- A workout stays in progress until it is finished. Starting a day again after finishing always begins fresh from the plan
- A days card on the profile, counting the days you trained
- The app can be reset to a clean slate from the bottom of the settings
- Uses its own application id, so it installs next to the original Kenko with separate data

### Changed

- Renamed to Kitae, with a new icon
- Existing plans are converted: every weekday becomes a day named after it, with sets planned from the last time each exercise was performed. Past sessions are linked to these days
- Statistics, history and suggestions only count sets that were done
- Sessions show the name of their day, and more than one workout can be logged on the same date
- Sets of an ongoing workout can be edited by tapping them
- Picking a plan to follow brings you back to the home screen

### Removed

- Plan templates without any exercises, only Push Pull Leg is included. Unused ones are removed when updating

### Fixed

- The welcome screen was skipped on the first start
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
