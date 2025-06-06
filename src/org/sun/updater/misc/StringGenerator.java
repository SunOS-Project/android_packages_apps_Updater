/*
 * Copyright (C) 2017 The LineageOS Project
 * Copyright (C) 2019 The PixelExperience Project
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
package org.sun.updater.misc;

import android.content.Context;
import android.content.res.Resources;

import org.sun.updater.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class StringGenerator {

    public static String getDateLocalizedUTC(Context context, int dateFormat, long unixTimestamp) {
        DateFormat f = DateFormat.getDateInstance(dateFormat, getCurrentLocale(context));
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(unixTimestamp * 1000);
        return f.format(date);
    }

    public static String getChangelogDate(long unixTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(unixTimestamp * 1000);
        return sdf.format(date);
    }

    public static String formatETA(Context context, long millis) {
        final long SECOND_IN_MILLIS = 1000;
        final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
        final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
        Resources res = context.getResources();
        if (millis >= HOUR_IN_MILLIS) {
            final int hours = (int) ((millis + 1800000) / HOUR_IN_MILLIS);
            return res.getQuantityString(R.plurals.eta_hours, hours, hours);
        } else if (millis >= MINUTE_IN_MILLIS) {
            final int minutes = (int) ((millis + 30000) / MINUTE_IN_MILLIS);
            return res.getQuantityString(R.plurals.eta_minutes, minutes, minutes);
        } else {
            final int seconds = (int) ((millis + 500) / SECOND_IN_MILLIS);
            return res.getQuantityString(R.plurals.eta_seconds, seconds, seconds);
        }
    }

    private static Locale getCurrentLocale(Context context) {
        return context.getResources().getConfiguration().getLocales()
                .getFirstMatch(context.getResources().getAssets().getLocales());
    }
}
