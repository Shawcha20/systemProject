plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.android") // Added this to ensure Kotlin plugin is applied
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.educationappsysproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.educationappsysproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    sourceSets {
        getByName("main") {
            java {
                srcDirs("src\\main\\java", "src\\main\\java\\2")
            }
        }
    }
}


// shawcha added


//repositories{
//        mavenCentral()
//}
val navVersion = "2.2.1"
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)








    //pdf section
//    implementation("com.google.firebase:firebase-storage:21.0.0")
//    implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")
//    implementation ("androidx.recyclerview:recyclerview:1.2.1")
//    androidTestImplementation ("androidx.test:runner:1.3.0")
//    testImplementation ("androidx.test:core:1.3.0")
//    testImplementation ("org.mockito:mockito-core:3.6.28")
//    testImplementation ("androidx.test.ext:junit:1.1.2")
//    implementation("androidx.activity:activity:1.8.0")
//
//    implementation("com.google.firebase:firebase-firestore:25.0.0")
//    implementation("com.google.firebase:firebase-database:21.0.0")




    //exam section
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("androidx.navigation:navigation-fragment:$navVersion")
    implementation("androidx.navigation:navigation-ui:$navVersion")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))


//
//    // sweet alert
   // implementation("com.github.f0ris.sweetalert:library:1.5.1");
}
