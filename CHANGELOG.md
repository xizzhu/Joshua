CHANGELOG
---------

#### Next Release
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
