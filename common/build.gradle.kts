import com.google.protobuf.gradle.*
import org.gradle.internal.impldep.com.fasterxml.jackson.core.JsonPointer.compile

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("com.android.library")
    id("org.jetbrains.kotlin.native.cocoapods")
    id("com.squareup.sqldelight")
    id("com.chromaticnoise.multiplatform-swiftpackage") version "2.0.3"
    idea
    id("com.google.protobuf")
    id("kotlin-kapt")
}

repositories {
    jcenter()
}

// CocoaPods requires the podspec to have a version.
version = "1.0"
val protobufVersion = "3.6.1"
val pbandkVersion = "0.9.1" //by extra("0.9.1")

android {
    compileSdkVersion(AndroidSdk.compile)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}


// workaround for https://youtrack.jetbrains.com/issue/KT-43944
android {
    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}


kotlin {
    val sdkName: String? = System.getenv("SDK_NAME")

    val isiOSDevice = sdkName.orEmpty().startsWith("iphoneos")
    if (isiOSDevice) {
        iosArm64("iOS")
    } else {
        iosX64("iOS")
    }

    val isWatchOSDevice = sdkName.orEmpty().startsWith("watchos")
    if (isWatchOSDevice) {
        watchosArm64("watch")
    } else {
        watchosX86("watch")
    }

    macosX64("macOS")
    android()
    jvm()

    cocoapods {
        // Configure fields required by CocoaPods.
        summary = "Some description for a Kotlin/Native module"
        homepage = "Link to a Kotlin/Native module homepage"
    }

    js {
        browser {
        }
    }

    sourceSets {

        sourceSets["commonMain"].dependencies {
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}") {
                isForce = true
            }

            // Ktor
            implementation(Ktor.clientCore)
            implementation(Ktor.clientJson)
            implementation(Ktor.clientLogging)
            implementation(Ktor.clientSerialization)

            // Kotlinx Serialization
            implementation(Serialization.core)

            // SQL Delight
            implementation(SqlDelight.runtime)
            implementation(SqlDelight.coroutineExtensions)

            // For the `common` sourceset in a Kotlin Multiplatform project:
            implementation("pro.streem.pbandk:pbandk-runtime:0.9.1")

            // koin
            api(Koin.core)
            api(Koin.test)

            // kermit
            api(Deps.kermit)

            // kroto+
//            implementation("com.google.api.grpc:proto-google-common-protos:1.16.0")
        }
        sourceSets["commonTest"].dependencies {
        }

        sourceSets["androidMain"].dependencies {
            implementation(Ktor.clientAndroid)
            implementation(SqlDelight.androidDriver)
        }
        sourceSets["androidTest"].dependencies {
            implementation(kotlin("test-junit"))
            implementation(Test.junit)
        }

        sourceSets["jvmMain"].dependencies {
            implementation(Ktor.clientApache)
            implementation(Ktor.slf4j)
            implementation(SqlDelight.jdbcDriver)
            implementation(SqlDelight.sqlliteDriver)

            // For Kotlin/JVM sourcesets/projects:
//            implementation("pro.streem.pbandk:pbandk-runtime-jvm:0.9.1")

            // Service gen
//            implementation("pro.streem.pbandk:protoc-gen-kotlin-lib-jvm:0.9.1")
        }

        sourceSets["iOSMain"].dependencies {
            implementation(Ktor.clientIos)
            implementation(SqlDelight.nativeDriver)

//            // For Kotlin/Native sourcesets/projects:
//            implementation("pro.streem.pbandk:pbandk-runtime-native:0.9.1")
        }
        sourceSets["iOSTest"].dependencies {
        }

        sourceSets["watchMain"].dependencies {
            implementation(Ktor.clientIos)
            implementation(SqlDelight.nativeDriver)
        }

        sourceSets["macOSMain"].dependencies {
            implementation(Ktor.clientCio)
            implementation(SqlDelight.nativeDriverMacos)
        }

        sourceSets["jsMain"].dependencies {
            implementation(Ktor.clientJs)

//            // For Kotlin/JS sourcesets/projects:
//            implementation("pro.streem.pbandk:pbandk-runtime-js:0.9.1")
        }
    }
}


sqldelight {
    database("PeopleInSpaceDatabase") {
        packageName = "com.surrus.peopleinspace.db"
        sourceFolders = listOf("sqldelight")
    }
}

multiplatformSwiftPackage {
    packageName("PeopleInSpace")
    swiftToolsVersion("5.3")
    targetPlatforms {
        iOS { v("13") }
    }
}

//tasks {
//    compileJava {
//        enabled = false
//    }
//
//    withType<KotlinCompile>().configureEach {
//        kotlinOptions {
//            jvmTarget = "1.8"
//            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
//        }
//    }
//}

protobuf {
    generatedFilesBaseDir = "$projectDir/generated"

    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.25.0"
        }
        id("vertx") {
            artifact = "io.vertx:vertx-grpc-protoc-plugin:4.0.0" // ${vertx.grpc.version}
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.0.0:jdk7@jar"
        }
        id("kotlin") {
            artifact = "pro.streem.pbandk:protoc-gen-kotlin-jvm:$pbandkVersion:jvm8@jar"
        }
    }
    generateProtoTasks {
//        ofSourceSet("main")
        all().forEach { task ->
//            task.dependsOn ":kp-scripts:jar"
//            task.inputs.file(krotoConfig)
            task.generateDescriptorSet = true
            task.descriptorSetOptions.includeSourceInfo = true
            task.descriptorSetOptions.includeImports = true

//            task.builtins {
//                remove("java")
//            }

            task.plugins {
                id("grpc")
                id("grpckt")
                id("vertx")
                id("kotlin") {
                    option("kotlin_package=test.example")
                }
            }

            task.doFirst {
                delete(protobuf.protobuf.generatedFilesBaseDir)
            }
        }
    }
}

tasks["clean"].doFirst {
    delete(protobuf.protobuf.generatedFilesBaseDir)
}

//idea.module {
//    sourceDirs.add(file("${protobuf.protobuf.generatedFilesBaseDir}/main/java"))
//    sourceDirs.add(file("${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"))
//}

//sourceSets {
//    main {
//        java.srcDirs(
//            "${protobuf.protobuf.generatedFilesBaseDir}/main/java",
//            "${protobuf.protobuf.generatedFilesBaseDir}/main/grpc"
//        )
//    }
//}

// Workaround the Gradle bug resolving multi-platform dependencies.
// Fix courtesy of https://github.com/square/okio/issues/647
configurations.forEach {
    if (it.name.toLowerCase().contains("kapt") || it.name.toLowerCase().contains("proto")) {
        it.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
    }
}