import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    // Enforce explicit API visibility and explicit return types for SDK design
    explicitApi()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "AuthSdk"
            isStatic = true
        }
    }

    androidLibrary {
        namespace = "com.arkamodh.authsdk.sdk"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        optimization {
            consumerKeepRules.apply {
                publish = true
                files.add(project.file("consumer-rules.pro"))
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        }
        androidMain.dependencies {
            implementation(dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.play.services.auth)
        }
    }
}

