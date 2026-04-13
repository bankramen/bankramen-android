import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.openapi.generator)
}

val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}
val apiBaseUrl = localProperties.getProperty("api.baseUrl", "https://api.example.com/")
val apiAuthToken = localProperties.getProperty("api.authToken", "")
val kakaoNativeAppKey = localProperties.getProperty("kakao.nativeAppKey", "")

tasks.register<GenerateTask>("generateBankramenApi") {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/openapi/bankramen-api.yaml")
    outputDir.set(generatedOpenApiDir.get().asFile.path)
    apiPackage.set("com.uson.myapplication.generated.api")
    modelPackage.set("com.uson.myapplication.generated.model")
    packageName.set("com.uson.myapplication.generated")
    configFile.set("$rootDir/openapi/openapi-generator-config.json")
    validateSpec.set(true)
}

android {
    namespace = "com.uson.myapplication"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.uson.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "API_AUTH_TOKEN", "\"$apiAuthToken\"")
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
        manifestPlaceholders["kakaoScheme"] = "kakao$kakaoNativeAppKey"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            java.srcDir(generatedOpenApiDir.map { it.dir("src/main/kotlin") })
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.named("preBuild") {
    dependsOn("generateBankramenApi")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.squareup.retrofit)
    implementation(libs.squareup.retrofit.converter.gson)
    implementation(libs.squareup.retrofit.converter.scalars)
    implementation(libs.squareup.okhttp)
    implementation(libs.squareup.okhttp.logging)
    implementation(libs.google.gson)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kakao.user)
    implementation(libs.kakao.auth)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
