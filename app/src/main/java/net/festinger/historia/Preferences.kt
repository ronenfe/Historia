package net.festinger.historia

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat


class Preferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}