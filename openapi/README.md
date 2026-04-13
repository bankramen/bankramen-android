# OpenAPI automation

- Replace `bankramen-api.yaml` with the real Swagger/OpenAPI spec, or point the Gradle task to your spec file.
- Generate sources with `./gradlew :app:generateBankramenApi`.
- Generated Kotlin client code is emitted to `app/build/generated/openapi`.
- Handwritten app code should stay outside the generated package.
