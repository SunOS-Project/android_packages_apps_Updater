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
package org.nameless.updates.misc;

import static org.nameless.vibrator.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_MISC_SCENES;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.nameless.updates.controller.UpdaterService;
import org.nameless.updates.model.Update;
import org.nameless.updates.model.UpdateBaseInfo;
import org.nameless.updates.model.UpdateInfo;
import org.nameless.updates.model.UpdateStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    private static final String TAG = "Utils";

    private Utils() {
    }

    public static File getDownloadPath() {
        return new File(Constants.DOWNLOAD_PATH);
    }

    public static File getExportPath() {
        File dir = new File(Environment.getExternalStorageDirectory(), Constants.EXPORT_PATH);
        if (!dir.isDirectory()) {
            if (dir.exists() || !dir.mkdirs()) {
                throw new RuntimeException("Could not create directory");
            }
        }
        return dir;
    }

    public static File getCachedUpdateList(Context context) {
        return new File(context.getCacheDir(), "updates.json");
    }

    // This should really return an UpdateBaseInfo object, but currently this only
    // used to initialize UpdateInfo objects
    private static UpdateInfo parseJsonUpdate(JSONObject object, Context context) throws JSONException {
        Update update = new Update();
        update.setTimestamp(object.getLong("date"));
        update.setRequiredDate(object.getLong("requiredDate"));
        update.setName(object.getString("filename"));
        update.setDownloadId(object.getString("id"));
        update.setFileSize(object.getLong("filesize"));
        update.setDownloadUrl(object.getString("url"));
        update.setVersion(object.getString("version"));
        update.setHash(object.getString("md5"));
        return update;
    }

    public static boolean isCompatible(UpdateBaseInfo update) {
        if (!(update.getVersion().equalsIgnoreCase(getVersion()))) {
            Log.d(TAG, update.getName() + " with version " + update.getVersion() + " is different from current Android version " + getVersion());
            return false;
        }
        if (update.getRequiredDate() > getBuildDate()) {
            Log.d(TAG, update.getName() + " requires build date " + update.getRequiredDate() + " is newer than current build date " + getBuildDate());
            return false;
        }
        return true;
    }

    public static boolean isNewUpdate(UpdateBaseInfo update) {
        return update.getTimestamp() > getBuildDate();
    }

    public static UpdateInfo parseJson(File file, Context context)
            throws IOException, JSONException {

        StringBuilder json = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                json.append(line);
            }
        }

        JSONObject obj = new JSONObject(json.toString());
        try {
            UpdateInfo update = parseJsonUpdate(obj, context);
            if (isNewUpdate(update)) {
                return update;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse update object", e);
        }

        return null;
    }

    private static long getBuildDate() {
        return SystemProperties.getLong(Constants.PROP_BUILD_DATE, 0) * 1000;
    }

    public static String getVersion() {
        return SystemProperties.get(Constants.PROP_BUILD_VERSION);
    }

    public static String getDevice() {
        return SystemProperties.get(Constants.PROP_DEVICE);
    }

    public static String getJsonURL() {
        return String.format(Constants.OTA_URL, getVersion(), getDevice());
    }

    public static String getChangelog(Context context, long unixTimestamp) {
        String url = getChangelogURL(context, unixTimestamp);
        String changelog = readChangelogFromUrl(url);
        if (changelog != "" && changelog != null) {
            return changelog;
        }
        url = getChangelogURL(unixTimestamp);
        changelog = readChangelogFromUrl(url);
        return changelog;
    }

    private static String getChangelogURL(long unixTimestamp) {
        return String.format(Constants.CHANGELOG_URL, getVersion(), getDevice(), StringGenerator.getChangelogDate(unixTimestamp));
    }

    private static String getChangelogURL(Context context, long unixTimestamp) {
        return String.format(Constants.CHANGELOG_URL_LOCALE, getVersion(), getDevice(), StringGenerator.getChangelogDate(unixTimestamp),
                context.getResources().getConfiguration().locale.getLanguage(),
                context.getResources().getConfiguration().locale.getCountry());
    }

    public static void triggerUpdate(Context context) {
        final Intent intent = new Intent(context, UpdaterService.class);
        intent.setAction(UpdaterService.ACTION_INSTALL_UPDATE);
        context.startService(intent);
    }

    public static void rebootDevice(Context mContext) {
        PowerManager pm =
                (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    public static boolean isNetworkMetered(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.isActiveNetworkMetered();
    }

    public static boolean checkForNewUpdates(File oldJson, File newJson, boolean fromBoot, Context context)
            throws IOException, JSONException {
        if (!oldJson.exists() || fromBoot) {
            return parseJson(newJson, context) != null;
        }
        UpdateInfo oldUpdate = parseJson(oldJson, context);
        UpdateInfo newUpdate = parseJson(newJson, context);
        if (oldUpdate == null || newUpdate == null) {
            return false;
        }
        return !oldUpdate.getDownloadId().equals(newUpdate.getDownloadId());
    }

    public static long getZipEntryOffset(ZipFile zipFile, String entryPath) {
        // Each entry has an header of (30 + n + m) bytes
        // 'n' is the length of the file name
        // 'm' is the length of the extra field
        final int FIXED_HEADER_SIZE = 30;
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        long offset = 0;
        while (zipEntries.hasMoreElements()) {
            ZipEntry entry = zipEntries.nextElement();
            int n = entry.getName().length();
            int m = entry.getExtra() == null ? 0 : entry.getExtra().length;
            int headerSize = FIXED_HEADER_SIZE + n + m;
            offset += headerSize;
            if (entry.getName().equals(entryPath)) {
                return offset;
            }
            offset += entry.getCompressedSize();
        }
        Log.e(TAG, "Entry " + entryPath + " not found");
        throw new IllegalArgumentException("The given entry was not found");
    }

    private static void removeUncryptFiles(File downloadPath) {
        File[] uncryptFiles = downloadPath.listFiles(
                (dir, name) -> name.endsWith(Constants.UNCRYPT_FILE_EXT));
        if (uncryptFiles == null) {
            return;
        }
        for (File file : uncryptFiles) {
            Log.d(TAG, "Deleting " + file.getAbsolutePath());
            try {
                file.delete();
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete " + file.getAbsolutePath(), e);
            }
        }
    }

    public static void cleanupDownloadsDir(Context context) {
        File downloadPath = getDownloadPath();
        removeUncryptFiles(downloadPath);
        Log.d(TAG, "Cleaning " + downloadPath);
        if (!downloadPath.isDirectory()) {
            return;
        }
        File[] files = downloadPath.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            Log.d(TAG, "Deleting " + file.getAbsolutePath());
            try{
                file.delete();
            }catch (Exception e){
                Log.e(TAG, "Failed to delete " + file.getAbsolutePath(), e);
            }
        }
    }

    public static boolean isABDevice() {
        return SystemProperties.getBoolean(Constants.PROP_AB_DEVICE, false);
    }

    public static boolean isEncrypted(Context context, File file) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (sm == null) {
            return false;
        }
        return sm.isEncrypted(file);
    }

    public static long getUpdateCheckInterval() {
        return AlarmManager.INTERVAL_DAY;
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public static String readableFileSize(long size) {
        String[] units = new String[]{"B", "kB", "MB", "GB", "TB", "PB"};
        int mod = 1024;
        double power = (size > 0) ? Math.floor(Math.log(size) / Math.log(mod)) : 0;
        String unit = units[(int) power];
        double result = size / Math.pow(mod, power);
        if (unit.equals("B") || unit.equals("kB") || unit.equals("MB")) {
            result = (int) result;
            return String.format("%d %s", (int) result, unit);
        }
        return String.format("%01.2f %s", result, unit);
    }

    public static int getPersistentStatus(Context context){
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(Constants.PREF_CURRENT_PERSISTENT_STATUS, UpdateStatus.Persistent.UNKNOWN);
    }

    @SuppressLint("ApplySharedPref")
    public static void setPersistentStatus(Context context, int status){
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(Constants.PREF_CURRENT_PERSISTENT_STATUS, status).commit();
    }

    public static void doHapticFeedback(Context context, Vibrator vibrator) {
        if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.get(VibrationEffect.EFFECT_CLICK), VIBRATION_ATTRIBUTES_MISC_SCENES);
        }
    }

    public static String readChangelogFromUrl(String url) {
        String res = "";
        try {
            URL getUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) getUrl.openConnection();
            conn.setRequestProperty("User-Agent", "org.nameless.updates");
            conn.setConnectTimeout(3000);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = new String(line.getBytes(), "utf-8");
                if (sb.toString() == "") {
                    sb.append(line);
                } else {
                    sb.append("\n" + line);
                }
            }
            reader.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
