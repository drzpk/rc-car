package dev.drzepka.arduino.rc_car.controller.activity.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import dev.drzepka.arduino.rc_car.controller.R

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen?): RecyclerView.Adapter<*> {
        return object : PreferenceGroupAdapter(preferenceScreen) {

            @SuppressLint("RestrictedApi")
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)

                val preference = getItem(position)
                if (preference is SeekBarPreference)
                    modifySeekBarPreference(preference, holder)
            }
        }
    }

    private fun modifySeekBarPreference(preference: SeekBarPreference, holder: PreferenceViewHolder) {
        if (!(preference.key == SettingsAccessor.MINIMUM_SPEED_PREFERENCE || preference.key == SettingsAccessor.MAXIMUM_TURN_RATIO_PREFERENCE))
            return

        preference.javaClass.getDeclaredField("mSeekBarValueTextView").let {
            it.isAccessible = true
            it.set(preference, null)
        }

        val seekBarText = holder.findViewById(androidx.preference.R.id.seekbar_value) as TextView
        seekBarText.text = String.format("%d%%", preference.value * 10)

        preference.setOnPreferenceChangeListener { _, newValue ->
            seekBarText.text = String.format("%d%%", (newValue as Int) * 10)
            true
        }
    }
}