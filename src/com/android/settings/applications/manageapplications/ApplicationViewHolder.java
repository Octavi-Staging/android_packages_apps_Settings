/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.applications.manageapplications;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;

public class ApplicationViewHolder extends RecyclerView.ViewHolder {

    @VisibleForTesting
    final TextView mAppName;
    @VisibleForTesting
    final TextView mSummary;
    @VisibleForTesting
    final TextView mDisabled;
    @VisibleForTesting
    final ViewGroup mWidgetContainer;
    @VisibleForTesting
    final Switch mSwitch;

    private final ImageView mAppIcon;

    ApplicationViewHolder(View itemView) {
        super(itemView);
        mAppName = itemView.findViewById(android.R.id.title);
        mAppIcon = itemView.findViewById(android.R.id.icon);
        mSummary = itemView.findViewById(android.R.id.summary);
        mDisabled = itemView.findViewById(R.id.appendix);
        mSwitch = itemView.findViewById(R.id.switchWidget);
        mWidgetContainer = itemView.findViewById(android.R.id.widget_frame);
    }

    static View newView(ViewGroup parent) {
        return newView(parent, false /* twoTarget */);
    }

    static View newView(ViewGroup parent, boolean twoTarget) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.preference_app, parent, false);
        final ViewGroup widgetFrame = view.findViewById(android.R.id.widget_frame);
        if (twoTarget) {
            if (widgetFrame != null) {
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_widget_primary_switch, widgetFrame, true);

                View divider = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.preference_two_target_divider, view, false);
                // second to last, before widget frame
                view.addView(divider, view.getChildCount() - 1);
            }
        } else if (widgetFrame != null) {
            widgetFrame.setVisibility(View.GONE);
        }
        return view;
    }

    static View newHeader(ViewGroup parent, int resText) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.preference_app_header, parent, false);
        TextView textView = view.findViewById(R.id.apps_top_intro_text);
        textView.setText(resText);
        return view;
    }

    void setSummary(CharSequence summary) {
        mSummary.setText(summary);
        updateSummaryVisibility();
    }

    void setSummary(@StringRes int summary) {
        mSummary.setText(summary);
        updateSummaryVisibility();
    }

    private void updateSummaryVisibility() {
        // Hide an empty summary and then title will be vertically centered.
        mSummary.setVisibility(TextUtils.isEmpty(mSummary.getText()) ? View.GONE : View.VISIBLE);
    }

    void setEnabled(boolean isEnabled) {
        itemView.setEnabled(isEnabled);
    }

    void setTitle(CharSequence title, CharSequence contentDescription) {
        if (title == null) {
            return;
        }
        mAppName.setText(title);

        if (TextUtils.isEmpty(contentDescription)) {
            return;
        }
        mAppName.setContentDescription(contentDescription);
    }

    void setIcon(int drawableRes) {
        mAppIcon.setImageResource(drawableRes);
    }

    void setIcon(Drawable icon) {
        if (icon == null) {
            return;
        }
        mAppIcon.setImageDrawable(icon);
    }

    void updateDisableView(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
            mDisabled.setVisibility(View.VISIBLE);
            mDisabled.setText(R.string.not_installed);
        } else if (!info.enabled || info.enabledSetting
                == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            mDisabled.setVisibility(View.VISIBLE);
            mDisabled.setText(R.string.disabled);
        } else if ((info.privateFlags & ApplicationInfo.PRIVATE_FLAG_HIDDEN) != 0) {
            mDisabled.setVisibility(View.VISIBLE);
            mDisabled.setText(R.string.hidden);
        } else {
            mDisabled.setVisibility(View.GONE);
        }
    }

    void updateSizeText(AppEntry entry, CharSequence invalidSizeStr, int whichSize) {
        if (ManageApplications.DEBUG) {
            Log.d(ManageApplications.TAG, "updateSizeText of "
                    + entry.label + " " + entry + ": " + entry.sizeStr);
        }
        if (entry.sizeStr != null) {
            switch (whichSize) {
                case ManageApplications.SIZE_INTERNAL:
                    setSummary(entry.internalSizeStr);
                    break;
                case ManageApplications.SIZE_EXTERNAL:
                    setSummary(entry.externalSizeStr);
                    break;
                default:
                    setSummary(entry.sizeStr);
                    break;
            }
        } else if (entry.size == ApplicationsState.SIZE_INVALID) {
            setSummary(invalidSizeStr);
        }
    }

    void updateSwitch(Switch.OnCheckedChangeListener listener, boolean enabled, boolean checked) {
        if (mSwitch != null && mWidgetContainer != null) {
            mWidgetContainer.setFocusable(false);
            mWidgetContainer.setClickable(false);
            mSwitch.setFocusable(true);
            mSwitch.setClickable(true);
            mSwitch.setOnCheckedChangeListener(listener);
            mSwitch.setChecked(checked);
            mSwitch.setEnabled(enabled);
        }
    }
}
