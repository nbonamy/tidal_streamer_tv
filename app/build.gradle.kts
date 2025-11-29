plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "fr.bonamy.tidalstreamer"
  compileSdk = 35

  defaultConfig {
    applicationId = "fr.bonamy.tidalstreamer"
    minSdk = 29
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"
  }

  signingConfigs {
    create("release") {
      keyAlias = "app"
      keyPassword = "tidal00"
      storeFile = file("../keystore.jks")
      storePassword = "tidal00"
    }
  }

  buildTypes {
    debug {
      signingConfig = signingConfigs.getByName("release")
    }
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.leanback)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.palette.ktx)
  implementation(libs.androidx.transition)
  implementation(libs.glide)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  implementation(libs.converter.gson)
  implementation(libs.miniequalizer)
  implementation(libs.zxing.core)

}