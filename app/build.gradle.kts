plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
}

android {
	namespace = "fr.bonamy.tidalstreamer"
	compileSdk = 35

	defaultConfig {
		applicationId = "fr.bonamy.tidalstreamer"
		minSdk = 31
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
	implementation(libs.glide)
	implementation(libs.retrofit)
	implementation(libs.converter.gson)
}