plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.preactivated.preactivate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.preactivated.preactivate"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //....Circular Image View....
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //...Firebase...........
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    //....Authentication......
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth")
    //.....Google Auth.........
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    //....Image Carousel.............
    implementation ("org.imaginativeworld.whynotimagecarousel:whynotimagecarousel:2.1.0")
    //....Glide Dependency (For Image)........
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    //......Firebase Firestore...................
    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.3")
    //.....Lottie Animation...............
    implementation ("com.airbnb.android:lottie:6.2.0")
    implementation ("com.google.firebase:firebase-storage-ktx")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    //......Justified Text box
    implementation ("jp.wasabeef:richeditor-android:2.0.0")


}