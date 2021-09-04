package dev.drzepka.arduino.rc_car.controller.activity.settings

import android.content.Context
import androidx.preference.PreferenceManager

class SettingsAccessor(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSettings(): SettingValues {
        return SettingValues(
            preferences.getInt(MINIMUM_SPEED_PREFERENCE, 60),
            preferences.getInt(MAXIMUM_TURN_RATIO_PREFERENCE, 60),
            preferences.getInt(POWER_DECREASE_PREFERENCE, 0)
        )
    }

    companion object {
        const val MINIMUM_SPEED_PREFERENCE = "minimum_speed"
        const val MAXIMUM_TURN_RATIO_PREFERENCE = "maximum_turn_ratio"
        const val POWER_DECREASE_PREFERENCE = "power_decrease"
    }

    data class SettingValues(val minimumSpeed: Int, val maximumTurnRatio: Int, val powerDecrease: Int)
}