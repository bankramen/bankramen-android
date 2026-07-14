skills:
- omo:ulw-loop: requested by user; evidence-bound run/QA workflow.
- android-cli: Android project execution and AVD discovery.
tier: LIGHT - read-only execution of existing Gradle/OpenAPI/API test surfaces; no code edits, schema changes, auth changes, or new integration.
criteria:
- C001: run generated OpenAPI client task via ./gradlew :app:generateBankramenApi; PASS if BUILD SUCCESSFUL and generated API files exist.
- C002: run API request coverage test via ./gradlew :app:testDebugUnitTest --tests com.uson.myapplication.core.api.BankramenApiCoverageTest; PASS if test task succeeds.
- C003: inspect AVD availability via android emulator list + adb devices; PASS if available AVD/device state is captured, or blocked if no device can be run.
