# OpenAPI automation

- `bankramen-api.json` is based on `https://bankramen-api.cloud/v3/api-docs`.
- The local file removes the duplicate `JWT` bearer scheme and renames the `LocalTime` component to `BankramenLocalTime` so OpenAPI Generator emits valid Kotlin.
- Generate sources with `./gradlew :app:generateBankramenApi`.
- Generated Kotlin client code is emitted to `app/build/generated/openapi`.
- Handwritten app code should stay outside the generated package.
