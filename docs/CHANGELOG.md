CHANGELOG
---------

#### Next Release
- Bug fixes:
  - Used SupervisorJob for parent scope
  - Should close activity when failed to load annotated verses, reading progress, and Strong's numbers
- Refactoring
  - Used ViewPager2 instead of ViewPager
  - Used Lifecycle and ViewModel from AndroidX
- Updated dependencies:
  - Kotlin JVM target to 1.8
  - Gradle to 6.1.1
  - Android Gradle plugin to 3.6.0
  - Android build tools to 29.0.3
  - AndroidX Core to 1.2.0, Lifecycle to 2.2.0, Material to 1.1.0, ViewPager2 to 1.0.0
  - Dagger to 2.26
  - Mockito to 3.3.0

#### v0.14.0 (2020-01-19)
- New features:
  - Initial support for Strong's numbers
- Bug fixes:
  - Properly cancel translation download / removal and dismiss dialog on stop
  - Should call AndroidReadingProgressStorage.save() on IO thread
  - Do not fail if the requested verse does not exist when loading annotated verses
  - Do not fail when reading a large number of verses by verse indexes
  - Should use elapsedRealTime() when tracking reading time
  - Used coroutineScope() + async() instead
- Updated dependencies:
  - Gradle to 6.1
  - Firebase Core and Analytics to 17.2.2
  - Dagger to 2.25.4

#### v0.13.2 (2019-12-29)
- Improvements:
  - No longer log ChildCancelledException to Firebase
- Bug fixes:
  - Should dismiss translation download / removal dialog on destroy

#### v0.13.1 (2019-12-27)
- Bug fixes:
  - Disabled R8 full mode due to a crash

#### v0.13.0 (2019-12-27)
- New features:
  - Saved parallel translations to disk
  - Supported cancelling translation downloading
  - Supported search history
  - Supported instant search
  - Supported hiding search button
- Bug fixes:
  - Should close verse detail when changing current verse index
  - Should call backup() when backup fails
  - Caught OutOfMemoryError when it fails to restore
  - Should start / stop reading progress tracking on resume / pause
  - Properly checked if root exception is JobCancellationException
- Improvements:
  - Better organized managers and repositories
  - Read verses in verse annotation presenter in batch
- Updated dependencies:
  - Dagger to 2.25.3
  - Mockito to 3.2.4

#### v0.12.8 (2019-12-14)
- Updated dependencies:
  - Kotlin Coroutine to 1.3.3
  - ASK to 0.5.1
  - Logger to 0.1.5

#### v0.12.7 (2019-12-13)
- Should sort search result by book index

#### v0.12.6 (2019-12-11)
- Properly handles case when translation downloading is cancelled
  - As a result, reverted "Only emit if book names and book short names are loaded"
- Throw exception when missing translation list

#### v0.12.5 (2019-12-07)
- Do nothing if it's already downloading or removing translations
- Fixed searching with single quote
- Updated dependencies:
  - Android Gradle plugin to 3.5.3
  - ASK to 0.5.0

#### v0.12.4 (2019-12-04)
- No longer report coroutine JobCancellationException to Crashlytics
- Only emit if book names and book short names are loaded
- Updated dependencies:
  - AndroidX Coordinator Layout to 1.1.0
  - Mockito to 3.2.0

#### v0.12.3 (2019-12-01)
- Used ASK to access SQLite
- Should not show dialog when activity is destroyed
- Updated dependencies:
  - Gradle to 6.0.1
  - Kotlin to 1.3.61
  - AndroidX Recycler View to 1.1.0
  - ASK to 0.4.0

#### v0.12.2 (2019-11-13)
- Added logs to better understand crash in ChapterListAdapter
- Updated dependencies:
  - Play Services plugin to 4.3.3

#### v0.12.1 (2019-11-11)
- Should dismiss dialog when translation presenter is stopped
- Updated dependencies:
  - Grardle to 6.0

#### v0.12.0 (2019-11-08)
- Major refactoring to use clean architecture
- Updated dependencies:
  - Grardle to 5.6.4
  - Android Gradle Plugin to 3.5.2
  - Dagger to 2.25.2
  - Firebase Analytics and Core to 17.2.1
  - Mockito to 3.1.0

#### v0.11.1 (2019-09-27)
- Fixed backup with large data

#### v0.11.0 (2019-09-26)
- Supported backup and restore
- Updated dependencies:
  - Gradle to 5.6.2
  - Kotlin Coroutine to 1.3.2
  - AndroidX AppCompat to 1.1.0
  - Play Services plugin to 4.3.2

#### v0.10.1 (2019-09-03)
- Fixed issue with parallel verses loading (#104)
- Fixed scrolling position in case of following empty verses
- Fixed issue when loading verse detail with following empty verses (#108)
- Updated dependencies:
  - Grardle to 5.6.1
  - Firebase Core and Analytics to 17.2.0

#### v0.10.0 (2019-08-26)
- Improved R8 configurations:
  - Used proguard-android-optimize.txt
  - Do not keep unnecessary AndroidX classes
- Updated dependencies:
  - Android Gradle plugin to 3.5.0
  - Coveralls plugin to 2.8.3
  - Kotlin to 1.3.50
  - Kotlin Coroutine to 1.3.0
  - Play Services plugin to 4.3.1
  - Firebase Core and Analytics to 17.1.0
  - Logger to 0.1.4

#### v0.9.1 (2019-08-14)
- Harmonized styles for verses rendering
- Should wait for 2.5 seconds before tracking reading progress
- Should not track reading progress when no translation is available
- Should not fail when a parallel translation is not available
- Should hide keyboard when verse detail is closed
- Updated dependencies:
  - Grardle to 5.6

#### v0.9.0 (2019-08-10)
- Improved UI for reading
  - Better handling in case of empty verse (especially useful for MSG)
  - Fixed highlight update under simple reading mode
- Used URLConnection instead of Retrofit
- Should reload when user deletes translations
- Updated dependencies:
  - Build tool to 29.0.2
  - Dagger to 2.24

#### v0.8.1 (2019-07-21)
- Fixed initialization of channels inside managers

#### v0.8.0 (2019-07-20)
- Supported highlight list
- Improved translation list sorting
- Made book names in verse detail view, bookmark lists bold and smaller
- Should set hint on TextInputLayout
- Should always notify loading started / finished for reading progress, bookmarks, and notes
- Enabled R8 full mode
- Updated dependencies:
  - Gradle to 5.5.1
  - Android Gradle plugin to 3.4.2
  - Build tool to 29.0.1
  - Kotlin to 1.3.41
  - Firebase Core and Analytics to 17.0.1
  - OkHttp to 4.0.1
  - Logger to 0.1.2
  - Mockito to 3.0.0

#### v0.7.0 (2019-07-02)
- Added verse highlight support
- Grouped available translations by language
- Should not fail saving when translation table already exists
- Updated dependencies:
  - Gradle to 5.5
  - Kotlin Coroutine to 1.2.2
  - OkHttp to 4.0.0

#### v0.6.0 (2019-06-20)
- Show per-chapter reading status in reading progress
- Fixed search result count
- Migrated to use Logger library
- Updated dependencies:
  - Firebase Analytics and Core to 17.0.0
  - Kotlin to 1.3.40
  - Dagger to 2.23.2

#### v0.5.1 (2019-06-10)
- Improved color for selected verses and verse details in night mode

#### v0.5.0 (2019-06-06)
- Supported sorting bookmarks and notes by book
- Divided search results by books
- Click verse detail to select translation and long click to copy
- Supported adaptive icons
- Only enabled Firebase crash and analytics reporting for release build
- Updated dependencies:
  - Build tool to 29.0.0
  - Compile and target SDK to 29
  - AndroidX Annotations to 1.1.0
  - AndroidX JUnit to 1.1.1
  - AndroidX Test Rules to 1.2.0
  - Crashlytics to 2.10.1
  - Dagger to 2.23.1
  - Retrofit to 2.6.0
  - Mockito to 2.28.2

#### v0.4.1 (2019-05-23)
- Added missing on click listener for simple reading mode
- Fixed copying on older devices where it only works on threads with looper

#### v0.4.0 (2019-05-22)
- Added "complex" reading mode to show bookmark & note with verse
- Added link to rate app on Play Store
- Notified user if translation lisst is empty
- Fixed padding for search result
- Fixed crash when failed to load translation list
- Removed Builder of Settings

#### v0.3.1 (2019-05-18)
- Should highlight selected verse when opening from notes
- Added INFO level logs to Crashlytics
- Fixed crash when downloading translation

#### v0.3.0 (2019-05-17)
- Added event tracking
  - Tracked translation download and installation time
  - Tracked search
- Open note when user opens reading activity from note list
- Should first try to close drawer
- Aligned title item UI with setting section header
- Lazy initialize Crashlytics
- Updated Android Gradle plugin to 3.4.1

#### v0.2.0 (2019-05-10)
- Added "no bookmark" and "no note" item
- Grouped bookmarks and notes by date
- Properly catch exception if thrown when downloading translations
- Refactored to introduce a common adapter
- Should keep action mode when user scrolls
- Fixed potential crash when failed to copy verses to clipboard
- Updated Firebase Core to 16.0.9 and Analytics to 16.5.0

#### v0.1.2 (2019-04-30)
- Improved verse detail
  - Highlight current selected verse when showing verse detail
  - Sorted verses by translation short name

#### v0.1.1 (2019-04-28)
- Fixes
  - Fixed crash when closing fectching translation stream
- Updated dependencies
  - Kotlin to 1.3.31
  - Kotlin Coroutine to 1.2.1

#### v0.1.0 (2019-04-26)
- Initial release
