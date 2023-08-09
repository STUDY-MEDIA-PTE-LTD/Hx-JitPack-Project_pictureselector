import org.gradle.api.JavaVersion

/*
 *
 *  * Copyright (C)  HuangLinqing, TravelPrevention Open Source Project
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

//版本号管理
open class AppVersions {
    companion object{
        const val buildToolsVersion = "33.0.0"
        const val compileSdkVersion = 33
        const val minSdkVersion = 21
        const val targetSdkVersion = 33

        const val versionCode = 1
        const val versionName = "1.0.1"

    }

}

//三方库管理
object AppLibs {
    const val appcompat = "androidx.appcompat:appcompat:1.3.1"
    const val gson = "com.google.code.gson:gson:2.8.2"
}