CHANGELOG
---------

#### Next Release
- Updated dependencies:
  - Firebase Core and Analytics to 17.1.0

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
