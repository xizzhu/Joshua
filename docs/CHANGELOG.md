CHANGELOG
---------

#### Next release
- Bug fixes:
  - Should call AppCompatDelegate.setDefaultNightMode() on main thread
- Update dependencies:
  - Android Gradle Plugin to 7.1.0
  - Android compile and target SDK level downgraded to 31
  - Robolectric to 4.7.3

#### v0.20.0 (2022-01-22)
- New features:
  - Support using system's day / night mode (#206)
  - Improve search functionality (#152)
    - Support searching from only Old Testament or New Testament
    - Use quotation mark for exact match
- Changes:
  - Refactor to use Material3 theme
  - Use Kover for code coverage
- Bug fixes:
  - Properly set Kotlin JVM target to Java 11
- Update dependencies:
  - Gradle to 7.3.3
  - Android Gradle plugin to 7.0.4
  - Android build tool to 32.0.0, compile and target SDK level to 32
  - Kotlin to 1.6.10, Coroutines to 1.6.0
  - AndroidX appcompat to 1.4.1, constraint layout to 2.1.3, coordinator layout to 1.2.0
  - Firebase BOM to 29.0.4, Crashlytics Gradle plugin to 2.8.1
  - Hilt to 2.40.5
  - Material Component to 1.5.0
  - Mockk to 1.12.2

#### v0.19.1 (2021-12-10)
- Bug fixes:
  - Should also remove caches when removing translations
  - Filter invalid Strong's Numbers

#### v0.19.0 (2021-11-26)
- New features:
  - Click anywhere on toolbar to open chapter selection view and move to current book (#206)
  - Add a seek bar to set font size scale (#206)
  - Long tap on search results, bookmarks, highlights, notes, and Strong's Numbers to show preview
  - Add "OT" and "NT" option to chapter selection view (#134)
  - Support resuming downloading translations and Strong's Numbers (#149)
- Changes:
  - Make view pager for reading less sensitive to swipe
- Bug fixes:
  - Set color of ActionMode's back arrow to white
  - Skip empty verses in Strong's Numbers
- Update dependencies:
  - Gradle to 7.3
  - Kotlin to 1.6.0
  - AndroidX Annotations to 1.3.0, AppCompat to 1.4.0
  - Hilt to 2.40.1
  - Mockk to 1.12.1

#### v0.18.2 (2021-11-11)
- Bug fixes:
  - Should not crash if currentSettingsViewData is not initialized yet
  - Fixed background color when night mode is on
  - Do not fail the search if a note is attached to a verse that does not exist in current translation

#### v0.18.1 (2021-11-05)
- Changes:
  - Remove Firebase Perf monitoring
- Bug fixes:
  - Should not crash if no translation is available

#### v0.18.0 (2021-10-31)
- Changes:
  - Lower Minimum SDK Version to 19 (Android 4.4)
  - Remove coveralls plugin
  - Use Roblectric for instrumented tests
  - Replace Mockito by Mockk (#201)
- Bug fixes:
  - Should run backup & restore on IO thread
- Update dependencies:
  - Gradle to 7.2
  - Android Gradle Plugin to 7.0.3
  - Compile and Target SDK to 31
  - Kotlin to 1.5.31, Coroutines to 1.5.2
  - AndroidX Activity to 1.4.0, Core to 1.7.0, Lifecycle to 1.4.0, Constraint Layout to 2.1.1
  - Firebase BOM 29.0.0, Google Services plugin to 4.3.10, Crashlytics plugin to 2.8.0
  - Hilt to 2.40
  - Logger to 0.6.1

#### v0.17.1 (2021-08-02)
- New features:
  - Add link to website
- Changes:
  - Re-enable proguard-android-optimize
- Bug fixes:
  - Properly update "consolidated sharing" setting switch
- Update dependencies:
  - Gradle to 7.1.1
  - Java to 11
  - Android Gradle plugin to 7.0.0, build tool to 31.0.0
  - Kotlin to 1.5.21, Coroutines to 1.5.1
  - AndroidX Activity to 1.3.0, Annotations to 1.2.0, AppCompat to 1.3.1, Core to 1.6.0, Lifecycle to 2.3.1, Constraint Layout to 2.1.0, Material Component to 1.4.0, Recycler View to 1.2.1, Espresso to 3.4.0, Test Rules to 1.4.0
  - Firebase plugin to 4.3.8, Analytics to 19.0.0, Crashlytics plugin to 2.7.1, Crashlytics to 18.2.0, Perf plugin to 1.4.0, Perf to 20.0.2
  - Dagger Hilt to 2.38.1
  - Mockito to 3.11.2

#### v0.17.0 (2021-02-19)
- New features:
  - Include bookmarks, highlights, and notes in search results
- Bug fixes:
  - Do not crash if it fails to set searchable info
  - Catch exception in case error occurred while observing downloaded translations
  - Show toast when failed to update bookmarks, highlights, and parallel translations
- Changes:
  - Use Firebase Perf to track translation and SN download and install time instead of Firebase Analytics
  - Remove Stetho (#198)
  - Use CrashlyticsLogger from Logger
- Refactoring:
  - Replace broadcast channel with state flow
- Update dependencies:
  - Gradle to 6.8.2
  - Android Gradle Plugin to 4.1.2, Build tool to 30.0.3
  - Kotlin to 1.4.30, Coroutines to 1.4.2
  - AndroidX Activity to 1.2.0, Core to 1.3.2, ConstraintLayout to 2.0.4, Lifecycle to 2.3.0, Material Components to 1.3.0
  - Firebase plugin to 4.3.5, Analytics to 18.0.2, Crashlytics to 17.3.1, Crashlytics plugin to 2.5.0, Perf to 19.1.1
  - Dagger Hilt to 2.32-alpha
  - Logger to 0.4.0
  - Mockito to 3.7.7

#### v0.16.2 (2020-09-04)
- Refactoring:
  - Migrate to use Hilt (#196)
- Bug fixes:
  - Use CopyOnWriteArrayList to store analytics providers
- Update dependencies:
  - Gradle to 6.6.1
  - Android build tool to 30.0.2
  - Kotlin:
    - Kotlin to 1.4.0
    - Coroutines to 1.3.9
  - AndroidX:
    - AppCompat to 1.2.0
    - ConstraintLayout to 2.0.0
    - DrawerLayout to 1.1.1
    - Material to 1.2.1
    - JUnit to 1.1.2
    - Test Rules to 1.3.0
    - Espresso to 3.3.0
  - Firebase:
    - Analytics to 17.5.0
    - Crashlytics plugin to 2.2.1, Crashlytics to 17.2.1
  - Dagger Hilt to 2.28.3-alpha
  - Logger to 0.3.0
  - Mockito to 3.5.10

#### v0.16.1 (2020-07-23)
- New features:
  - Add default color for highlight (#193)
- Bug fixes:
  - Add workaround for "unable to select texts from notes" (#191)
  - Should open note when reading activity is opened from note list
- Updated dependencies:
  - Android build tool to 30.0.1
  - Android Gradle plugin to 4.0.1
  - Compile SDK and target SDK to 30
  - Gradle to 6.5.1
  - Kotlin Coroutines to 1.3.8
  - AndroidX DrawerLayout to 1.1.0, SwipeRefreshLayout to 1.1.0
  - Firebase Analytics to 17.4.4, Core to 1.3.1, Crashlytics Gradle plugin to 2.2.0, Crashlytics to 17.1.1
  - Dagger to 2.28.3
  - Mockito to 3.4.4

#### v0.16.0 (2020-06-10)
- New features:
  - Allowed consolidating verses for sharing (#160)
- Bug fixes:
  - Fixed crashes when verses are loaded while user is scrolling the pages
- Updated dependencies:
  - Gradle to 6.5
  - Android Gradle plugin to 4.0.0
  - AndroidX Core to 1.3.0
  - Firebase Analytics to 17.4.3, Crashlytics Gradle plugin to 2.1.1, Crashlytics to 17.0.1
  - Dagger to 2.28

#### v0.15.1 (2020-05-24)
- Bug fixes:
  - Properly handled error case when downloading & removing translations, and downloading Strong's numbers

#### v0.15.0 (2020-05-22)
- Bug fixes:
  - Used SupervisorJob for parent scope
  - Should close activity when failed to load annotated verses, reading progress, and Strong's numbers
  - Properly nullify when jobs updating annotated verses complete
  - Should scroll after chapter group is expanded / collapsed
  - Fixed issue with Direct Share on Android 10 and above
- Changes:
  - No longer show book name when simple reading mode is off
  - Added confirmation dialog before downloading
  - Tweaked text to show translation name when confirming removal
- Refactoring:
  - Migrated to Firebase Crashlytics
  - Used ViewPager2 instead of ViewPager
  - Used Lifecycle and ViewModel from AndroidX
  - No longer needs ViewData
- Updated dependencies:
  - Kotlin JVM target to 1.8
  - Kotlin to 1.3.72, Coroutine to 1.3.7
  - Gradle to 6.4.1
  - Android Gradle plugin to 3.6.3
  - Android build tools to 29.0.3
  - AndroidX Core to 1.2.0, Lifecycle to 2.2.0, Material to 1.1.0, ViewPager2 to 1.0.0
  - Firebase Analytics to 17.4.1, Crashlytics to 17.0.0, Crashlytics Gradle plugin to 2.1.0, removed Firebase Core
  - Dagger to 2.27
  - ASK to 0.5.2
  - Logger to 0.2.0
  - Mockito to 3.3.3

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
