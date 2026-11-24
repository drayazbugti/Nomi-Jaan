package com.example.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.R
import com.example.model.AppThemeMode
import com.example.model.ExportResolution
import com.example.model.SavedCreation
import com.example.model.StudioPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.studioDataStore: DataStore<Preferences> by preferencesDataStore(name = "bugta_editz_prefs")

class StudioRepository(private val context: Context) {

    private object Keys {
        val SAVED_CREATIONS_JSON = stringPreferencesKey("saved_creations_json")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_WHITE_ON_CLOSE = booleanPreferencesKey("auto_white_on_close")
        val AUTO_SAVE_DRAFTS = booleanPreferencesKey("auto_save_drafts")
        val CLOUD_AUTO_BACKUP = booleanPreferencesKey("cloud_auto_backup")
        val CREDITS = intPreferencesKey("studio_credits")
        val DEFAULT_RESOLUTION = stringPreferencesKey("default_resolution")
        val AUTO_FACE_LOCK = booleanPreferencesKey("auto_face_lock")
        val SHOW_COMPARE_HINT = booleanPreferencesKey("show_compare_hint")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LOGGED_IN_EMAIL = stringPreferencesKey("logged_in_email")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        val FAVORITE_FONT_IDS = stringPreferencesKey("favorite_font_ids")
    }

    val savedCreationsFlow: Flow<List<SavedCreation>> = context.studioDataStore.data.map { prefs ->
        if (!prefs.contains(Keys.SAVED_CREATIONS_JSON)) {
            defaultStarterDrafts()
        } else {
            parseCreations(prefs[Keys.SAVED_CREATIONS_JSON])
        }
    }

    val preferencesFlow: Flow<StudioPreferences> = context.studioDataStore.data.map { prefs ->
        val resName = prefs[Keys.DEFAULT_RESOLUTION] ?: ExportResolution.UHD_4K.name
        val resolution = runCatching { ExportResolution.valueOf(resName) }
            .getOrDefault(ExportResolution.UHD_4K)
        val themeName = prefs[Keys.THEME_MODE] ?: AppThemeMode.WHITE.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeName) }
            .getOrDefault(AppThemeMode.WHITE)

        StudioPreferences(
            themeMode = themeMode,
            autoWhiteOnClose = prefs[Keys.AUTO_WHITE_ON_CLOSE] ?: false,
            autoSaveDrafts = prefs[Keys.AUTO_SAVE_DRAFTS] ?: true,
            cloudAutoBackup = prefs[Keys.CLOUD_AUTO_BACKUP] ?: false,
            credits = prefs[Keys.CREDITS] ?: 25,
            defaultResolution = resolution,
            autoFaceLock = prefs[Keys.AUTO_FACE_LOCK] ?: true,
            showBeforeAfterHint = prefs[Keys.SHOW_COMPARE_HINT] ?: true,
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            loggedInEmail = prefs[Keys.LOGGED_IN_EMAIL].orEmpty(),
            profileName = prefs[Keys.PROFILE_NAME].orEmpty(),
            profilePhotoPath = prefs[Keys.PROFILE_PHOTO_PATH].orEmpty(),
            favoriteFontIds = prefs[Keys.FAVORITE_FONT_IDS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: setOf("sacramento", "cinzel_bold", "playfair_display")
        )
    }

    private suspend fun getCurrentList(prefs: Preferences): MutableList<SavedCreation> {
        return if (!prefs.contains(Keys.SAVED_CREATIONS_JSON)) {
            defaultStarterDrafts().toMutableList()
        } else {
            parseCreations(prefs[Keys.SAVED_CREATIONS_JSON]).toMutableList()
        }
    }

    suspend fun addSavedCreation(creation: SavedCreation) {
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            current.add(0, creation)
            val trimmed = current.take(40)
            prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(trimmed)
        }
    }

    suspend fun removeCreation(id: String) {
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs).filterNot { it.id == id }
            prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(current)
        }
    }

    suspend fun removeCreations(ids: Set<String>) {
        if (ids.isEmpty()) return
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs).filterNot { it.id in ids }
            prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(current)
        }
    }

    suspend fun renameCreation(id: String, newTitle: String) {
        val clean = newTitle.trim()
        if (clean.isEmpty()) return
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs).map { item ->
                if (item.id == id) item.copy(title = clean) else item
            }
            prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(current)
        }
    }

    suspend fun duplicateCreation(id: String, newId: String) {
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs)
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                val original = current[index]
                val copy = original.copy(
                    id = newId,
                    title = "${original.title} (Copy)",
                    isUploadedToCloud = false,
                    createdAtMillis = System.currentTimeMillis()
                )
                current.add(0, copy)
                prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(current.take(40))
            }
        }
    }

    suspend fun markUploadedToCloud(ids: Set<String>) {
        if (ids.isEmpty()) return
        context.studioDataStore.edit { prefs ->
            val current = getCurrentList(prefs).map { item ->
                if (item.id in ids) item.copy(isUploadedToCloud = true) else item
            }
            prefs[Keys.SAVED_CREATIONS_JSON] = serializeCreations(current)
        }
    }

    suspend fun clearHistory() {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.SAVED_CREATIONS_JSON] = "[]"
        }
    }

    suspend fun updatePreferences(preferences: StudioPreferences) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = preferences.themeMode.name
            prefs[Keys.AUTO_WHITE_ON_CLOSE] = preferences.autoWhiteOnClose
            prefs[Keys.AUTO_SAVE_DRAFTS] = preferences.autoSaveDrafts
            prefs[Keys.CLOUD_AUTO_BACKUP] = preferences.cloudAutoBackup
            prefs[Keys.CREDITS] = preferences.credits
            prefs[Keys.DEFAULT_RESOLUTION] = preferences.defaultResolution.name
            prefs[Keys.AUTO_FACE_LOCK] = preferences.autoFaceLock
            prefs[Keys.SHOW_COMPARE_HINT] = preferences.showBeforeAfterHint
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun toggleFavoriteFont(fontId: String) {
        context.studioDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_FONT_IDS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableSet()
                ?: mutableSetOf("sacramento", "cinzel_bold", "playfair_display")
            if (fontId in current) {
                current.remove(fontId)
            } else {
                current.add(fontId)
            }
            prefs[Keys.FAVORITE_FONT_IDS] = current.joinToString(",")
        }
    }

    suspend fun setAuthSession(
        isLoggedIn: Boolean,
        email: String?,
        defaultDisplayName: String?
    ) {
        context.studioDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
            if (isLoggedIn) {
                if (!email.isNullOrBlank()) {
                    prefs[Keys.LOGGED_IN_EMAIL] = email
                }
                val existingName = prefs[Keys.PROFILE_NAME].orEmpty()
                if (existingName.isBlank() && !defaultDisplayName.isNullOrBlank()) {
                    prefs[Keys.PROFILE_NAME] = defaultDisplayName.trim()
                }
            } else {
                prefs[Keys.LOGGED_IN_EMAIL] = ""
            }
        }
    }

    suspend fun saveProfileName(name: String) {
        val clean = name.trim()
        context.studioDataStore.edit { prefs ->
            prefs[Keys.PROFILE_NAME] = clean
        }
    }

    suspend fun saveProfilePhotoFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        val avatarDir = File(context.filesDir, "profile_avatars").apply { mkdirs() }
        val targetFile = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.exists() && targetFile.length() > 0L
        }.getOrDefault(false)

        if (!copied) {
            runCatching { targetFile.delete() }
            return@withContext null
        }

        // Remove older avatar files so only the latest remains on disk
        avatarDir.listFiles()?.forEach { old ->
            if (old.absolutePath != targetFile.absolutePath) {
                runCatching { old.delete() }
            }
        }

        context.studioDataStore.edit { prefs ->
            prefs[Keys.PROFILE_PHOTO_PATH] = targetFile.absolutePath
        }
        targetFile.absolutePath
    }

    suspend fun clearProfilePhoto() {
        withContext(Dispatchers.IO) {
            val avatarDir = File(context.filesDir, "profile_avatars")
            avatarDir.listFiles()?.forEach { runCatching { it.delete() } }
        }
        context.studioDataStore.edit { prefs ->
            prefs[Keys.PROFILE_PHOTO_PATH] = ""
        }
    }

    suspend fun resetToWhiteThemeIfConfigured() {
        val prefsSnapshot = context.studioDataStore.data.first()
        val autoWhite = prefsSnapshot[Keys.AUTO_WHITE_ON_CLOSE] ?: false
        if (autoWhite) {
            context.studioDataStore.edit { prefs ->
                prefs[Keys.THEME_MODE] = AppThemeMode.WHITE.name
            }
        }
    }

    private fun defaultStarterDrafts(): List<SavedCreation> {
        val now = System.currentTimeMillis()
        return listOf(
            SavedCreation(
                id = "starter_cinema_4k",
                title = "Night Cinema 4K Draft",
                uriString = "",
                isVideo = false,
                resolutionLabel = "4K Ultra HD",
                modeLabel = "Ultra HD",
                createdAtMillis = now - 3_600_000L,
                isUploadedToCloud = false,
                sampleResId = R.drawable.img_showcase_cinema
            ),
            SavedCreation(
                id = "starter_portrait_glow",
                title = "Velvet Portrait Retouch",
                uriString = "",
                isVideo = false,
                resolutionLabel = "1080p HD",
                modeLabel = "Portrait",
                createdAtMillis = now - 7_200_000L,
                isUploadedToCloud = true,
                sampleResId = R.drawable.img_demo_portrait
            )
        )
    }

    private fun parseCreations(rawJson: String?): List<SavedCreation> {
        if (rawJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(rawJson)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val sampleId = obj.optInt("sampleResId", 0).takeIf { it != 0 }
                    add(
                        SavedCreation(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            uriString = obj.optString("uriString", ""),
                            isVideo = obj.optBoolean("isVideo", false),
                            resolutionLabel = obj.optString("resolutionLabel", "1080p HD"),
                            modeLabel = obj.optString("modeLabel", "Ultra HD"),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                            isUploadedToCloud = obj.optBoolean("isUploadedToCloud", false),
                            sampleResId = sampleId
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun serializeCreations(items: List<SavedCreation>): String {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("uriString", item.uriString)
                put("isVideo", item.isVideo)
                put("resolutionLabel", item.resolutionLabel)
                put("modeLabel", item.modeLabel)
                put("createdAtMillis", item.createdAtMillis)
                put("isUploadedToCloud", item.isUploadedToCloud)
                if (item.sampleResId != null) {
                    put("sampleResId", item.sampleResId)
                }
            }
            array.put(obj)
        }
        return array.toString()
    }
}
