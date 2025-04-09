package com.ocr.myapplication.sharedpreference
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import com.ocr.myapplication.R

class AppPreferences(context: Context) {
    // SharedPreferences file name
    companion object {
        private const val PREFS_NAME = "MyAppPreferences"

        // Keys for all preferences
        private const val KEY_BG_IMAGE = "background_image"
        private const val KEY_CHAT_BG_IMAGE = "chat_background_image"
        private const val KEY_OTHER_BG_IMAGE = "other_background_image"
        private const val KEY_APP_ICON = "app_icon"
        private const val KEY_REMINDER_VOLUME = "reminder_volume"
        private const val KEY_REMINDER_SOUND = "reminder_sound"

        // Default values
        @DrawableRes
        private val DEFAULT_BG_IMAGE = R.drawable.color_screen6
        @DrawableRes
        private val DEFAULT_CHAT_BG_IMAGE = R.drawable.chatscreen
        @DrawableRes
        private val DEFAULT_OTHER_BG_IMAGE = R.drawable.color_screen6
        @DrawableRes
        private val DEFAULT_APP_ICON = R.drawable.icon_1
        private const val DEFAULT_REMINDER_VOLUME = 80 // 0-100 scale
        private const val DEFAULT_REMINDER_SOUND = "default_sound.mp3"
    }

    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // CREATE/UPDATE methods
    fun setBackgroundImage(@DrawableRes resId: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_BG_IMAGE, resId)
            apply()
        }
    }

    fun setChatBackgroundImage(@DrawableRes resId: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_CHAT_BG_IMAGE, resId)
            apply()
        }
    }

    fun setOtherBackgroundImage(@DrawableRes resId: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_OTHER_BG_IMAGE, resId)
            apply()
        }
    }

    fun setAppIcon(@DrawableRes resId: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_APP_ICON, resId)
            apply()
        }
    }

    fun setReminderVolume(volume: Int) {
        with(sharedPref.edit()) {
            putInt(KEY_REMINDER_VOLUME, volume.coerceIn(0, 100))
            apply()
        }
    }

    fun setReminderSound(soundName: String) {
        with(sharedPref.edit()) {
            putString(KEY_REMINDER_SOUND, soundName)
            apply()
        }
    }

    // READ methods
    @DrawableRes
    fun getBackgroundImage(): Int {
        return sharedPref.getInt(KEY_BG_IMAGE, DEFAULT_BG_IMAGE)
    }

    // READ methods
    @DrawableRes
    fun getChatBackgroundImage(): Int {
        return sharedPref.getInt(KEY_CHAT_BG_IMAGE, DEFAULT_CHAT_BG_IMAGE)
    }

    // READ methods
    @DrawableRes
    fun getOtherBackgroundImage(): Int {
        return sharedPref.getInt(KEY_OTHER_BG_IMAGE, DEFAULT_OTHER_BG_IMAGE)
    }

    // READ methods
    @DrawableRes
    fun getAppIcon(): Int {
        return sharedPref.getInt(KEY_APP_ICON, DEFAULT_APP_ICON)
    }

    fun getReminderVolume(): Int {
        return sharedPref.getInt(KEY_REMINDER_VOLUME, DEFAULT_REMINDER_VOLUME)
    }

    fun getReminderSound(): String {
        return sharedPref.getString(KEY_REMINDER_SOUND, DEFAULT_REMINDER_SOUND) ?: DEFAULT_REMINDER_SOUND
    }

    // DELETE methods
    fun clearBackgroundImage() {
        with(sharedPref.edit()) {
            remove(KEY_BG_IMAGE)
            apply()
        }
    }

    fun clearChatBackgroundImage() {
        with(sharedPref.edit()) {
            remove(KEY_CHAT_BG_IMAGE)
            apply()
        }
    }

    fun clearOtherBackgroundImage() {
        with(sharedPref.edit()) {
            remove(KEY_OTHER_BG_IMAGE)
            apply()
        }
    }

    fun clearAppIcon() {
        with(sharedPref.edit()) {
            remove(KEY_APP_ICON)
            apply()
        }
    }

    fun clearReminderVolume() {
        with(sharedPref.edit()) {
            remove(KEY_REMINDER_VOLUME)
            apply()
        }
    }

    fun clearReminderSound() {
        with(sharedPref.edit()) {
            remove(KEY_REMINDER_SOUND)
            apply()
        }
    }

    // Clear all preferences
    fun clearAllPreferences() {
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }
}