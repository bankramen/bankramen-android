import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.net.URI
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.google.services)
}

val generatedOpenApiDir = layout.buildDirectory.dir("generated/openapi")
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name)
    ?: providers.gradleProperty(name).orNull
    ?: ""

fun String.toBuildConfigString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

val apiBaseUrl = localProperty("BaseUrl")
val apiHostIp = localProperty("api.hostIp")
val kakaoRedirectUri = localProperty("kakao.redirectUri").ifBlank { "bankramen://auth/kakao" }
val parsedKakaoRedirectUri = URI(kakaoRedirectUri)
val kakaoRedirectScheme = parsedKakaoRedirectUri.scheme ?: "bankramen"
val kakaoRedirectHost = parsedKakaoRedirectUri.host ?: "auth"
val kakaoRedirectPath = parsedKakaoRedirectUri.path?.takeIf { it.isNotBlank() } ?: "/kakao"

tasks.register<GenerateTask>("generateBankramenApi") {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/openapi/bankramen-api.json")
    outputDir.set(generatedOpenApiDir.get().asFile.path)
    apiPackage.set("com.uson.myapplication.generated.api")
    modelPackage.set("com.uson.myapplication.generated.model")
    packageName.set("com.uson.myapplication.generated")
    configFile.set("$rootDir/openapi/openapi-generator-config.json")
    validateSpec.set(true)
    doFirst {
        delete(generatedOpenApiDir)
    }
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
        buildConfigField("String", "API_BASE_URL", apiBaseUrl.toBuildConfigString())
        buildConfigField("String", "API_HOST_IP", apiHostIp.toBuildConfigString())
        buildConfigField("String", "KAKAO_REDIRECT_URI", kakaoRedirectUri.toBuildConfigString())
        manifestPlaceholders["kakaoRedirectScheme"] = kakaoRedirectScheme
        manifestPlaceholders["kakaoRedirectHost"] = kakaoRedirectHost
        manifestPlaceholders["kakaoRedirectPath"] = kakaoRedirectPath
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
    ksp(libs.androidx.room.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
