/*
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.custom.ambient.display;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settingslib.collapsingtoolbar.R;

import com.voltage.support.preferences.SystemSettingSeekBarPreference;
import com.voltage.support.preferences.SystemSettingSwitchPreference;
import com.voltage.support.preferences.SecureSettingSwitchPreference;

public class DozeSettings extends CollapsingToolbarBaseActivity implements PreferenceFragment.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, getNewFragment())
                    .commit();
        }
    }

    private PreferenceFragment getNewFragment() {
        return new MainSettingsFragment();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment,
            Preference preference) {
        Fragment instantiate = Fragment.instantiate(this, preference.getFragment(),
            preference.getExtras());
        getFragmentManager().beginTransaction().replace(
                R.id.content_frame, instantiate).addToBackStack(preference.getKey()).commit();

        return true;
    }

    public static class MainSettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private Context mContext;

        private static final String KEY_CATEGORY_GESTURES = "gestures";

        private PreferenceCategory mGesturesCategory;

        private SwitchPreferenceCompat mAoDPreference;
        private SwitchPreferenceCompat mAmbientDisplayPreference;
        private SwitchPreferenceCompat mPickUpPreference;
        private SwitchPreferenceCompat mHandwavePreference;
        private SwitchPreferenceCompat mPocketPreference;
        private SecureSettingSwitchPreference mDozeOnChargePreference;
        private SecureSettingSwitchPreference mMusicTickerPreference;
        private SystemSettingSeekBarPreference mDozeBrightness;
        private SystemSettingSeekBarPreference mPulseBrightness;
        private SystemSettingSwitchPreference mDoubleTapPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            setPreferencesFromResource(R.xml.doze_settings, rootKey);

            mContext = getActivity();

            mAoDPreference =
                (SwitchPreferenceCompat) findPreference(Utils.AOD_KEY);

            mDozeOnChargePreference =
                (SecureSettingSwitchPreference) findPreference(Utils.AOD_CHARGE_KEY);

            mGesturesCategory =
                (PreferenceCategory) findPreference(KEY_CATEGORY_GESTURES);
            mDoubleTapPreference =
                (SystemSettingSwitchPreference) findPreference(Utils.DOUBLE_TAP_KEY);

            if (Utils.isTapToWakeAvailable(mContext)) {
                mDoubleTapPreference.setOnPreferenceChangeListener(this);
            } else {
                if (mGesturesCategory != null) {
                    mGesturesCategory.removePreference(mDoubleTapPreference);
                }
            }

            mMusicTickerPreference =
                (SecureSettingSwitchPreference) findPreference(Utils.MUSIC_TICKER_KEY);
            mGesturesCategory =
                (PreferenceCategory) findPreference(KEY_CATEGORY_GESTURES);

            if (Utils.isAoDAvailable(mContext)) {
                mAoDPreference.setChecked(Utils.isAoDEnabled(mContext));
                mAoDPreference.setOnPreferenceChangeListener(this);
                mDozeOnChargePreference.setChecked(Utils.isAoDChargeEnabled(mContext));
                mDozeOnChargePreference.setOnPreferenceChangeListener(this);
            } else {
                mAoDPreference.setVisible(false);
                mDozeOnChargePreference.setVisible(false);
            }

            mAmbientDisplayPreference =
                (SwitchPreferenceCompat) findPreference(Utils.AMBIENT_DISPLAY_KEY);
            mAmbientDisplayPreference.setChecked(Utils.isDozeEnabled(mContext));
            mAmbientDisplayPreference.setOnPreferenceChangeListener(this);

            mPickUpPreference =
                (SwitchPreferenceCompat) findPreference(Utils.PICK_UP_KEY);
            mPickUpPreference.setChecked(Utils.tiltGestureEnabled(mContext));
            mPickUpPreference.setOnPreferenceChangeListener(this);

            mHandwavePreference =
                (SwitchPreferenceCompat) findPreference(Utils.GESTURE_HAND_WAVE_KEY);
            mHandwavePreference.setChecked(Utils.handwaveGestureEnabled(mContext));
            mHandwavePreference.setOnPreferenceChangeListener(this);

            mPocketPreference =
                (SwitchPreferenceCompat) findPreference(Utils.GESTURE_POCKET_KEY);
            mPocketPreference.setChecked(Utils.pocketGestureEnabled(mContext));
            mPocketPreference.setOnPreferenceChangeListener(this);

            int defaultDoze = getResources().getInteger(
                    com.android.internal.R.integer.config_screenBrightnessDoze);
            int defaultPulse = getResources().getInteger(
                    com.android.internal.R.integer.config_screenBrightnessPulse);
            if (defaultPulse == -1) {
                defaultPulse = defaultDoze;
            }

            mPulseBrightness = (SystemSettingSeekBarPreference) findPreference(Utils.PULSE_BRIGHTNESS_KEY);
            int value = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.PULSE_BRIGHTNESS, defaultPulse);
            mPulseBrightness.setValue(value);
            mPulseBrightness.setOnPreferenceChangeListener(this);

            mDozeBrightness = (SystemSettingSeekBarPreference) findPreference(Utils.DOZE_BRIGHTNESS_KEY);
            value = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, defaultDoze);
            mDozeBrightness.setValue(value);
            mDozeBrightness.setOnPreferenceChangeListener(this);

            if (!getResources().getBoolean(R.bool.has_tilt_sensor)) {
                mPickUpPreference.setVisible(false);
            }

            if (!getResources().getBoolean(R.bool.has_proximity_sensor)) {
                mHandwavePreference.setVisible(false);
                mPocketPreference.setVisible(false);
            }

            if (mAoDPreference == null) return;
            setPrefs();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();

            if (Utils.AOD_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mAoDPreference.setChecked(value);
                Utils.enableAoD(value, mContext);
                setPrefs();
                return true;
            } else if (Utils.AOD_CHARGE_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mDozeOnChargePreference.setChecked(value);
                Utils.enableAoDCharge(value, mContext);
                setPrefs();
                return true;
            } else if (Utils.AMBIENT_DISPLAY_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mAmbientDisplayPreference.setChecked(value);
                Utils.enableDoze(value, mContext);
                return true;
            } else if (Utils.PICK_UP_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mPickUpPreference.setChecked(value);
                Utils.enablePickUp(value, mContext);
                return true;
            } else if (Utils.GESTURE_HAND_WAVE_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mHandwavePreference.setChecked(value);
                Utils.enableHandWave(value, mContext);
                return true;
            } else if (Utils.GESTURE_POCKET_KEY.equals(key)) {
                boolean value = (Boolean) newValue;
                mPocketPreference.setChecked(value);
                Utils.enablePocketMode(value, mContext);
                return true;
            } else if (preference == mPulseBrightness) {
                int value = (Integer) newValue;
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.PULSE_BRIGHTNESS, value);
                return true;
            } else if (preference == mDozeBrightness) {
                int value = (Integer) newValue;
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.DOZE_BRIGHTNESS, value);
                return true;
            } else if (Utils.DOUBLE_TAP_KEY.equals(key)) {
                if (!Utils.isTapToWakeEnabled(mContext)); {
                    Settings.Secure.putInt(mContext.getContentResolver(),
                            Settings.Secure.DOUBLE_TAP_TO_WAKE, 1);
                }
                return true;
            }
            return false;
        }

        private void setPrefs() {
            final boolean aodEnabled = Utils.isAoDEnabled(mContext);
            final boolean aodChargeEnabled = Utils.isAoDChargeEnabled(mContext);
            mAmbientDisplayPreference.setEnabled(!aodEnabled);
            mPickUpPreference.setEnabled(!aodEnabled);
            mHandwavePreference.setEnabled(!aodEnabled);
            mPocketPreference.setEnabled(!aodEnabled);
            mDozeOnChargePreference.setEnabled(!aodEnabled);
            mMusicTickerPreference.setEnabled(!aodEnabled);
            mDozeBrightness.setEnabled(aodEnabled || aodChargeEnabled);
            mPulseBrightness.setEnabled(!aodEnabled);
            mDoubleTapPreference.setEnabled(!aodEnabled);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }
}
