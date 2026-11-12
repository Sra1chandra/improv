# S.andy Android Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a personal Android app where you type story lines and S.andy replies in short yes-and turns, with stories stored on device.

**Architecture:** Three Compose screens over a Room database. Domain logic (letter, chaos die, prompt trim, send/retry) is pure Kotlin with JVM unit tests. The phone calls OpenAI Chat Completions directly. EncryptedSharedPreferences holds the API key. No backend.

**Tech Stack:** Kotlin, Jetpack Compose, Navigation Compose, Room, OkHttp, EncryptedSharedPreferences, JUnit 4 on JVM. Min SDK 26. Application id `app.sandy.improv`.

**Spec:** `docs/superpowers/specs/2026-11-12-improv-android-design.md`

---

## File map

```
app/src/main/java/app/sandy/improv/
  MainActivity.kt
  ImprovApp.kt
  domain/
    Letter.kt          # first-line → list avatar letter
    Chaos.kt           # 70/30 die
    Prompt.kt          # system prompt, trim, OpenAI messages
    SendPolicy.kt      # empty send ignore vs retry
    Role.kt            # YOU / SANDY
  data/
    Story.kt
    Message.kt
    AppDb.kt
    StoryDao.kt
    KeyStore.kt        # encrypted prefs: key + model
  net/
    OpenAi.kt          # completions + error mapping
  ui/
    Nav.kt
    PaperTheme.kt
    ChatsScreen.kt
    ThreadScreen.kt
    SettingsScreen.kt
app/src/test/java/app/sandy/improv/
  LetterTest.kt
  ChaosTest.kt
  PromptTest.kt
  SendPolicyTest.kt
  OpenAiParseTest.kt
```

Do not add search, voice, streaming, or a backend.

---

### Task 1: Gradle app shell

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/app/sandy/improv/MainActivity.kt`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/test/java/app/sandy/improv/SmokeTest.kt`
- Create: `gradle.properties`

- [ ] **Step 1: Write a failing JVM smoke test**

```kotlin
package app.sandy.improv

import org.junit.Assert.assertEquals
import org.junit.Test

class SmokeTest {
    @Test
    fun junitRuns() {
        assertEquals(4, 2 + 2)
    }
}
```

- [ ] **Step 2: Run it (expect FAIL: no project yet)**

Run: `./gradlew test`  
Expected: FAIL (gradle wrapper or module missing)

- [ ] **Step 3: Create the Android/Gradle shell**

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "sandy"
include(":app")
```

Root `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```

`gradle.properties`:

```
org.gradle.jvmargs=-Xmx2g
android.useAndroidX=true
kotlin.code.style=official
```

`app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp")
}
android {
    namespace = "app.sandy.improv"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.sandy.improv"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.foundation:foundation:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    testImplementation("junit:junit:4.13.2")
}
```

Generate wrapper if missing: `gradle wrapper --gradle-version 8.9`

`AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="false"
        android:label="S.andy"
        android:theme="@style/Theme.Sandy">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

`themes.xml`:

```xml
<resources>
    <style name="Theme.Sandy" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

`MainActivity.kt`:

```kotlin
package app.sandy.improv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Text("S.andy") }
    }
}
```

- [ ] **Step 4: Run smoke test**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.SmokeTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts build.gradle.kts app gradle.properties gradle/ gradlew gradlew.bat
git commit -m "chore: Android app shell for S.andy"
```

---

### Task 2: List letter from first line

**Files:**
- Create: `app/src/main/java/app/sandy/improv/domain/Letter.kt`
- Test: `app/src/test/java/app/sandy/improv/LetterTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.sandy.improv

import app.sandy.improv.domain.letterFromFirstLine
import org.junit.Assert.assertEquals
import org.junit.Test

class LetterTest {
    @Test fun forest() = assertEquals("I", letterFromFirstLine("I walk into the forest with my friend."))
    @Test fun skipsThe() = assertEquals("L", letterFromFirstLine("The laundromat is open at 2am."))
    @Test fun skipsA() = assertEquals("W", letterFromFirstLine("A waiter puts down the menu."))
    @Test fun skipsAn() = assertEquals("O", letterFromFirstLine("An owl opens the door."))
    @Test fun empty() = assertEquals("?", letterFromFirstLine("   "))
    @Test fun punctuation() = assertEquals("W", letterFromFirstLine("What are you doing with this gun?"))
}
```

- [ ] **Step 2: Run test, expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.LetterTest`  
Expected: FAIL (unresolved `letterFromFirstLine`)

- [ ] **Step 3: Implement**

```kotlin
package app.sandy.improv.domain

fun letterFromFirstLine(line: String): String {
    var t = line.trim()
    val prefixes = listOf("The ", "An ", "A ")
    for (p in prefixes) {
        if (t.length > p.length && t.startsWith(p, ignoreCase = true)) {
            t = t.substring(p.length).trim()
            break
        }
    }
    val ch = t.firstOrNull { it.isLetter() } ?: return "?"
    return ch.uppercaseChar().toString()
}
```

- [ ] **Step 4: Run tests, expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.LetterTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/sandy/improv/domain/Letter.kt app/src/test/java/app/sandy/improv/LetterTest.kt
git commit -m "feat: story list letter from first line"
```

---

### Task 3: Chaos die

**Files:**
- Create: `app/src/main/java/app/sandy/improv/domain/Chaos.kt`
- Test: `app/src/test/java/app/sandy/improv/ChaosTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.sandy.improv

import app.sandy.improv.domain.Chaos
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChaosTest {
    @Test
    fun alwaysChaosWhenRngIsZero() {
        assertTrue(Chaos { 0.0 }.roll())
    }

    @Test
    fun continueWhenRngIsPoint3() {
        assertFalse(Chaos { 0.30 }.roll())
    }

    @Test
    fun chaosWhenRngJustUnderThreshold() {
        assertTrue(Chaos { 0.299 }.roll())
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.ChaosTest`  
Expected: FAIL

- [ ] **Step 3: Implement**

```kotlin
package app.sandy.improv.domain

class Chaos(private val unit: () -> Double = { kotlin.random.Random.nextDouble() }) {
    fun roll(): Boolean = unit() < 0.30
}
```

- [ ] **Step 4: Run, expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.ChaosTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/sandy/improv/domain/Chaos.kt app/src/test/java/app/sandy/improv/ChaosTest.kt
git commit -m "feat: 30 percent chaos die on device"
```

---

### Task 4: Send policy (empty send)

**Files:**
- Create: `app/src/main/java/app/sandy/improv/domain/Role.kt`
- Create: `app/src/main/java/app/sandy/improv/domain/SendPolicy.kt`
- Test: `app/src/test/java/app/sandy/improv/SendPolicyTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.sandy.improv

import app.sandy.improv.domain.Role
import app.sandy.improv.domain.SendAction
import app.sandy.improv.domain.sendAction
import org.junit.Assert.assertEquals
import org.junit.Test

class SendPolicyTest {
    @Test
    fun emptyDoesNothingWhenThreadEmpty() {
        assertEquals(SendAction.Ignore, sendAction("", lastRole = null, lastFailed = false))
    }

    @Test
    fun emptyRetriesIfLastIsYouAndFailed() {
        assertEquals(SendAction.Retry, sendAction("  ", lastRole = Role.YOU, lastFailed = true))
    }

    @Test
    fun emptyIgnoredIfLastIsSandy() {
        assertEquals(SendAction.Ignore, sendAction("", lastRole = Role.SANDY, lastFailed = false))
    }

    @Test
    fun textSends() {
        assertEquals(SendAction.Send("hello"), sendAction(" hello ", lastRole = Role.SANDY, lastFailed = false))
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.SendPolicyTest`  
Expected: FAIL

- [ ] **Step 3: Implement**

```kotlin
package app.sandy.improv.domain

enum class Role { YOU, SANDY }

sealed class SendAction {
    data object Ignore : SendAction()
    data object Retry : SendAction()
    data class Send(val text: String) : SendAction()
}

fun sendAction(composer: String, lastRole: Role?, lastFailed: Boolean): SendAction {
    val t = composer.trim()
    if (t.isEmpty()) {
        return if (lastRole == Role.YOU && lastFailed) SendAction.Retry else SendAction.Ignore
    }
    return SendAction.Send(t)
}
```

- [ ] **Step 4: Run, expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.SendPolicyTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/sandy/improv/domain/Role.kt app/src/main/java/app/sandy/improv/domain/SendPolicy.kt app/src/test/java/app/sandy/improv/SendPolicyTest.kt
git commit -m "feat: empty send retries only after S.andy failed"
```

---

### Task 5: Prompt builder

**Files:**
- Create: `app/src/main/java/app/sandy/improv/domain/Prompt.kt`
- Test: `app/src/test/java/app/sandy/improv/PromptTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.sandy.improv

import app.sandy.improv.domain.ChatTurn
import app.sandy.improv.domain.PromptBuilder
import app.sandy.improv.domain.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptTest {
    private val turns = (1..20).map { i ->
        val role = if (i % 2 == 1) Role.YOU else Role.SANDY
        ChatTurn(role, "line $i")
    }

    @Test
    fun keepsOpeningLineWhenTrimming() {
        val msgs = PromptBuilder.trim(turns, keep = 6)
        assertEquals("line 1", msgs.first { it.role == Role.YOU }.text)
        assertEquals(6, msgs.size)
        assertEquals("line 20", msgs.last().text)
    }

    @Test
    fun chaosAddsHiddenInstruction() {
        val built = PromptBuilder.build(listOf(ChatTurn(Role.YOU, "A bear.")), chaos = true)
        assertTrue(built.any { it["content"]!!.contains("sharp turn") })
    }

    @Test
    fun continueHasNoChaosLine() {
        val built = PromptBuilder.build(listOf(ChatTurn(Role.YOU, "A bear.")), chaos = false)
        assertTrue(built.none { it["content"]!!.contains("sharp turn") })
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.PromptTest`  
Expected: FAIL

- [ ] **Step 3: Implement**

```kotlin
package app.sandy.improv.domain

data class ChatTurn(val role: Role, val text: String)

object PromptBuilder {
    const val SYSTEM = """
You are S.andy, a scene partner in a yes-and story. Not a chatbot.

Each reply: 1-3 short lines. At most 40 words. First line is the scene.
No preamble, recap, closer, markdown, lists, headings, or "as an AI".
Do not say "Yes, and" out loud.
No slop: tapestry, delve, vibrant, journey, "in this moment".
One new fact per turn. Match the user's length when they wrote one line.
Accept the shared world, then add. Be changed by what just happened.
If this is true, what else is true — one step only. Brick, not cathedral.
Characters may fight. You must not erase established facts.
"""

    const val CHAOS = "Keep every established fact. Add one sharp turn: a new problem, person, or place. Still one short reply."

    fun trim(turns: List<ChatTurn>, keep: Int = 24): List<ChatTurn> {
        if (turns.size <= keep) return turns
        val first = turns.first()
        val tail = turns.takeLast(keep - 1)
        val rest = if (tail.first().text == first.text && tail.first().role == first.role) tail.drop(1) else tail
        return listOf(first) + rest
    }

    fun build(turns: List<ChatTurn>, chaos: Boolean): List<Map<String, String>> {
        val out = mutableListOf<Map<String, String>>()
        out += mapOf("role" to "system", "content" to SYSTEM.trim())
        if (chaos) out += mapOf("role" to "system", "content" to CHAOS)
        for (t in trim(turns)) {
            val r = if (t.role == Role.YOU) "user" else "assistant"
            out += mapOf("role" to r, "content" to t.text)
        }
        return out
    }
}
```

- [ ] **Step 4: Run, expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.PromptTest`  
Expected: PASS (6 turns: opening `line 1` plus the last 5).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/sandy/improv/domain/Prompt.kt app/src/test/java/app/sandy/improv/PromptTest.kt
git commit -m "feat: S.andy system prompt, trim, chaos instruction"
```

---

### Task 6: OpenAI HTTP + error mapping

**Files:**
- Create: `app/src/main/java/app/sandy/improv/net/OpenAi.kt`
- Test: `app/src/test/java/app/sandy/improv/OpenAiParseTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package app.sandy.improv

import app.sandy.improv.net.OpenAi
import app.sandy.improv.net.SandyError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiParseTest {
    @Test
    fun readsAssistantText() {
        val json = """{"choices":[{"message":{"content":"He climbs."}}]}"""
        assertEquals("He climbs.", OpenAi.parseText(json))
    }

    @Test
    fun maps401() {
        assertEquals(SandyError.KEY_REFUSED, OpenAi.mapHttp(401))
    }

    @Test
    fun maps500() {
        assertEquals(SandyError.STUCK, OpenAi.mapHttp(500))
    }
}
```

- [ ] **Step 2: Run, expect FAIL**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.OpenAiParseTest`  
Expected: FAIL

- [ ] **Step 3: Implement parse + client**

```kotlin
package app.sandy.improv.net

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class SandyError { NO_KEY, KEY_REFUSED, OFFLINE, STUCK }

class SandyException(val error: SandyError) : Exception(error.name)

object OpenAi {
    fun parseText(body: String): String {
        val choices = JSONObject(body).getJSONArray("choices")
        return choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
    }

    fun mapHttp(code: Int): SandyError = when (code) {
        401, 403 -> SandyError.KEY_REFUSED
        in 500..599 -> SandyError.STUCK
        else -> SandyError.STUCK
    }

    fun complete(
        key: String,
        model: String,
        messages: List<Map<String, String>>,
        client: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(45, TimeUnit.SECONDS)
            .build(),
    ): String {
        if (key.isBlank()) throw SandyException(SandyError.NO_KEY)
        val arr = JSONArray()
        for (m in messages) {
            arr.put(JSONObject().put("role", m["role"]).put("content", m["content"]))
        }
        val payload = JSONObject()
            .put("model", model)
            .put("max_tokens", 80)
            .put("temperature", 0.9)
            .put("messages", arr)
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $key")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        try {
            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) throw SandyException(mapHttp(res.code))
                return parseText(body)
            }
        } catch (e: SandyException) {
            throw e
        } catch (e: IOException) {
            throw SandyException(SandyError.OFFLINE)
        }
    }
}
```

`org.json` is on Android. For JVM unit tests, add:

```kotlin
testImplementation("org.json:json:20240303")
```

to `app/build.gradle.kts`.

- [ ] **Step 4: Run, expect PASS**

Run: `./gradlew :app:testDebugUnitTest --tests app.sandy.improv.OpenAiParseTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/main/java/app/sandy/improv/net/OpenAi.kt app/src/test/java/app/sandy/improv/OpenAiParseTest.kt
git commit -m "feat: OpenAI completions client and error mapping"
```

---

### Task 7: Room + key store

**Files:**
- Create: `app/src/main/java/app/sandy/improv/data/Story.kt`
- Create: `app/src/main/java/app/sandy/improv/data/Message.kt`
- Create: `app/src/main/java/app/sandy/improv/data/StoryDao.kt`
- Create: `app/src/main/java/app/sandy/improv/data/AppDb.kt`
- Create: `app/src/main/java/app/sandy/improv/data/KeyStore.kt`
- Create: `app/src/main/java/app/sandy/improv/ImprovApp.kt`

No Room instrumented test in v1. Persistence is a manual check (spec item 2–3).

- [ ] **Step 1: Skip a fake failing test.** Write `KeyStore` interface used by ViewModels so tests can fake it later.

```kotlin
package app.sandy.improv.data

interface KeyStore {
    fun apiKey(): String
    fun setApiKey(value: String)
    fun model(): String
    fun setModel(value: String)
}
```

- [ ] **Step 2: Entities and DAO**

```kotlin
package app.sandy.improv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Story(
    @PrimaryKey val id: String,
    val title: String,
    val letter: String,
    val lastLine: String,
    val createdAt: Long,
    val updatedAt: Long,
)
```

```kotlin
package app.sandy.improv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey val id: String,
    val storyId: String,
    val role: String,
    val text: String,
    val createdAt: Long,
)
```

```kotlin
package app.sandy.improv.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM Story ORDER BY updatedAt DESC")
    fun stories(): Flow<List<Story>>

    @Query("SELECT * FROM Story WHERE id = :id")
    suspend fun story(id: String): Story?

    @Query("SELECT * FROM Message WHERE storyId = :id ORDER BY createdAt ASC")
    fun messages(id: String): Flow<List<Message>>

    @Query("SELECT * FROM Message WHERE storyId = :id ORDER BY createdAt ASC")
    suspend fun messagesOnce(id: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStory(story: Story)

    @Update
    suspend fun updateStory(story: Story)

    @Insert
    suspend fun insertMessage(message: Message)
}
```

```kotlin
package app.sandy.improv.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Story::class, Message::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun stories(): StoryDao
}
```

- [ ] **Step 3: Encrypted prefs (never log the key)**

```kotlin
package app.sandy.improv.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsKeyStore(ctx: Context) : KeyStore {
    private val prefs = EncryptedSharedPreferences.create(
        ctx,
        "sandy_secure",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun apiKey(): String = prefs.getString("api_key", "") ?: ""
    override fun setApiKey(value: String) { prefs.edit().putString("api_key", value.trim()).apply() }
    override fun model(): String = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
    override fun setModel(value: String) { prefs.edit().putString("model", value).apply() }
}
```

```kotlin
package app.sandy.improv

import android.app.Application
import androidx.room.Room
import app.sandy.improv.data.AppDb
import app.sandy.improv.data.PrefsKeyStore

class ImprovApp : Application() {
    lateinit var db: AppDb
        private set
    lateinit var keys: PrefsKeyStore
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDb::class.java, "sandy.db").build()
        keys = PrefsKeyStore(this)
    }
}
```

Add `android:name=".ImprovApp"` on `<application>` in the manifest.

- [ ] **Step 4: Compile**

Run: `./gradlew :app:assembleDebug`  
Expected: SUCCESS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/app/sandy/improv/data app/src/main/java/app/sandy/improv/ImprovApp.kt app/src/main/AndroidManifest.xml
git commit -m "feat: Room stories and encrypted API key"
```

---

### Task 8: Paper theme (no default purple)

**Files:**
- Create: `app/src/main/java/app/sandy/improv/ui/PaperTheme.kt`
- Create: `app/src/main/res/font/README.md` (download notes)

Kevin Sans is woff2. Android needs TTF.

- [ ] **Step 1: Vendor the font**

Download `https://kevintngo.github.io/looseleaf/fonts/KevinSans-Regular.woff2` and `KevinSans-Bold.woff2` into `/tmp`.

Convert:

```bash
pip install fonttools brotli
fonttools ttLib.woff2 decompress -o app/src/main/res/font/kevin_sans_regular.ttf /tmp/KevinSans-Regular.woff2
fonttools ttLib.woff2 decompress -o app/src/main/res/font/kevin_sans_bold.ttf /tmp/KevinSans-Bold.woff2
```

If convert fails, ship with `FontFamily.Serif` and a `ponytail:` comment: ceiling = not Kevin Sans; upgrade = vendor TTF.

- [ ] **Step 2: Theme tokens**

```kotlin
package app.sandy.improv.ui

import androidx.compose.ui.graphics.Color

object Ink {
    val desk = Color(0xFF9DB8AE)
    val paper = Color(0xFFF4EAD4)
    val felt = Color(0xFF2A241C)
    val you = Color(0xFF2A5F9E)
    val marker = Color(0xFFC45C3E)
    val pencil = Color(0xFF6B5E4E)
    val danger = Color(0xFFB42318)
    val umber = Color(0x385A371E)
}
```

Paper sheet composable: cream background, 1° tilt unless Settings or `reduceMotion`, short umber shadow, no Material purple `MaterialTheme` colorScheme. Wrap content in a custom `PaperTheme` that sets those colors. Stamp buttons: terracotta multiply-ish fill, ink outline.

- [ ] **Step 3: Compile**

Run: `./gradlew :app:assembleDebug`  
Expected: SUCCESS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/sandy/improv/ui/PaperTheme.kt app/src/main/res/font
git commit -m "feat: paper desk theme and Kevin Sans"
```

---

### Task 9: Three screens + navigation (static)

**Files:**
- Create: `app/src/main/java/app/sandy/improv/ui/Nav.kt`
- Create: `app/src/main/java/app/sandy/improv/ui/ChatsScreen.kt`
- Create: `app/src/main/java/app/sandy/improv/ui/ThreadScreen.kt`
- Create: `app/src/main/java/app/sandy/improv/ui/SettingsScreen.kt`
- Modify: `MainActivity.kt`

- [ ] **Step 1: Wire NavHost**

Routes: `chats`, `thread/{id}`, `settings`.

Chats: `LazyColumn` of stories, header "Chats" + gear pinned, `+ New story` stamp pinned at bottom, empty copy `No stories yet.`

Thread: back, title, `LazyColumn` reverse/scroll to last, bubbles:
- you: `Modifier.fillMaxWidth().padding(start = 22.percent)` + `wrapContentWidth(Alignment.End)` + blue
- sandy: `padding(end = 22.percent)` + `wrapContentWidth(Alignment.Start)` + ink
No avatars, no names. Composer empty, no placeholder. `…` when waiting. Error under composer in `Ink.danger`.

Settings: no tilt. Hidden key field. Model dropdown default `gpt-4o-mini`. Graphite note `Key stays on this phone.`

Use `LocalView.resources.configuration` / `AccessibilityManager` for reduced motion: tilt = 0.

- [ ] **Step 2: Point MainActivity at Nav**

Replace `Text("S.andy")` with `ImprovNav(...)`.

- [ ] **Step 3: Install on a device or emulator and click all three screens with fake in-memory lists if DB not hooked yet.** Prefer hooking DAO in the same task if time stays inside one sitting. If split: screens accept `List<Story>` props first, then Task 10 binds ViewModel.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/sandy/improv/ui app/src/main/java/app/sandy/improv/MainActivity.kt
git commit -m "feat: chats, thread, settings screens"
```

---

### Task 10: ViewModel send loop

**Files:**
- Create: `app/src/main/java/app/sandy/improv/ui/ThreadVm.kt`
- Create: `app/src/main/java/app/sandy/improv/ui/ChatsVm.kt`
- Create: `app/src/main/java/app/sandy/improv/ui/SettingsVm.kt`

- [ ] **Step 1: New story**

Generate `id` with `UUID.randomUUID()`. Insert empty story with title `""`, letter `"?"`, then navigate to thread. First successful you-message sets title, letter (`letterFromFirstLine`), lastLine.

- [ ] **Step 2: Send**

1. `sendAction` on composer.
2. Ignore → return.
3. Retry or Send: if Send, `insertMessage` role `you`.
4. If `apiKey()` blank → `SandyError.NO_KEY`, keep the you-message.
5. `Chaos().roll()` then `PromptBuilder.build(turns, chaos)`.
6. IO dispatcher `OpenAi.complete`.
7. Success: insert `sandy` message, update story `lastLine` + `updatedAt`.
8. Failure: set `lastFailed = true`, map error to the four copy strings:
   - NO_KEY → `add key in Settings`
   - KEY_REFUSED → `key refused`
   - OFFLINE → `offline`
   - STUCK → `S.andy is stuck`

Waiting: `…` row. Send disabled while in flight except we already disable until reply or error.

- [ ] **Step 3: Manual checks from the spec**

1. New story, one line, S.andy replies.
2. Kill app, story still there.
3. Key survives kill app.
4. Force chaos: temporarily `Chaos { 0.0 }`, confirm reply does not say the previous fact never happened.
5. Airplane mode: you-line remains, `offline`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/app/sandy/improv/ui
git commit -m "feat: send loop with chaos roll and on-device history"
```

---

### Task 11: Polish against Looseleaf pens

**Files:**
- Modify: `PaperTheme.kt`, `ChatsScreen.kt`, `ThreadScreen.kt`, `SettingsScreen.kt`

- [ ] **Step 1:** Stamp fill slightly inside outline. Desk background behind the sheet. Thread/Chats sheet tilt 1°. Settings tilt 0. Composer focus ring blue. No Material default FAB.

- [ ] **Step 2:** Play through on a phone. Pixel-check gutters (~22%).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/app/sandy/improv/ui
git commit -m "style: paper tooth, tilt, stamp pens"
```

---

## Self-review

Spec coverage:
- You vs S.andy sides + gutters + no heads → Task 9
- List letters → Task 2 + 10
- Scroll pinned chrome → Task 9
- Settings key/model → Task 7 + 9 + 10
- Chaos 30% on device → Task 3 + 10
- Prompt rules + trim + max_tokens 80 → Task 5 + 6
- Errors + empty retry → Task 4 + 6 + 10
- Room + encrypted key → Task 7
- Paper pens → Task 8 + 11
- Manual spec tests → Task 10 step 3

No voice, search, streaming, backend.

Types: `Role`, `ChatTurn`, `SandyError`, `SendAction`, `KeyStore` used consistently.
