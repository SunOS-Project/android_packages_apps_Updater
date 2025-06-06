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
package org.sun.updater.model;

import java.util.ArrayList;

public class UpdateBase implements UpdateBaseInfo {

    private String mName;
    private String mDownloadUrl;
    private String mDownloadId;
    private long mTimestamp;
    private long mRequiredDate;
    private String mVersion;
    private long mFileSize;
    private String mHash;

    UpdateBase() {
    }

    UpdateBase(UpdateBaseInfo update) {
        mName = update.getName();
        mDownloadUrl = update.getDownloadUrl();
        mDownloadId = update.getDownloadId();
        mTimestamp = update.getTimestamp();
        mRequiredDate = update.getRequiredDate();
        mVersion = update.getVersion();
        mFileSize = update.getFileSize();
    }

    @Override
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(String downloadId) {
        mDownloadId = downloadId;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    @Override
    public long getRequiredDate() {
        return mRequiredDate;
    }

    public void setRequiredDate(long requiredDate) {
        mRequiredDate = requiredDate;
    }

    @Override
    public String getVersion() {
        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    @Override
    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        mDownloadUrl = downloadUrl;
    }

    @Override
    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    @Override
    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }
}
