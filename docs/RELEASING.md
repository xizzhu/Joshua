Releasing
=========

 1. Bump the app version code in `Configurations.kt`.
 2. Update the `docs/CHANGELOG.md`.
 3. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 4. `git tag -a X.Y.Z -m "vX.Y.Z"` (where X.Y.Z is the new version)
 5. `./gradlew clean assembleRelease`
 6. `git push && git push --tags`

We use [Semantic Versioning](https://semver.org/).

Prerequisites
-------------

Create a `keystore.properties` file at project's root folder:
```
KEYSTORE_FILE=<keystore file>
KEYSTORE_PASSWORD=<keystore password>
KEY_ALIAS=<key alias>
KEY_PASSWORD=<key password>
```

More about app signing can be found [here](https://developer.android.com/studio/publish/app-signing).
