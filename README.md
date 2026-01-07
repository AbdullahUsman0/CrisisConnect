# CrisisConnect

Android app project. This repository contains the CrisisConnect Android application built with Gradle and Kotlin.

Quick start

- Build (Linux/macOS/Windows WSL or using included Gradle wrapper):
```bash
./gradlew assembleDebug
```

- Run unit tests:
```bash
./gradlew test
```

CI

A basic GitHub Actions workflow is included at `.github/workflows/android.yml` that builds the project on pushes and pull requests to `main`.

Notes

- If you see an embedded gitlink `CrisisConnect` in the repo index, it was likely an inner git repository added accidentally. To remove it from the outer repo's index (without deleting the folder), run:
```powershell
git rm --cached CrisisConnect
git commit -m "Remove embedded repo from index"
git push
```

- To protect branches or enable repository settings, open the repository on GitHub and configure branch protection rules under Settings → Branches.

Contact

If you want me to also configure branch protection rules or add more CI steps (lint, instrumented tests), reply and I can proceed.
