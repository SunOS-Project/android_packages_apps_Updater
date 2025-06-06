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

public enum UpdateStatus {
    UNKNOWN,
    STARTING,
    DOWNLOADING,
    DOWNLOADED,
    PAUSED,
    DOWNLOAD_ERROR,
    DELETED,
    VERIFYING,
    VERIFIED,
    VERIFICATION_FAILED,
    INSTALLING,
    INSTALLATION_FAILED;

    public static final class Persistent {
        public static final int UNKNOWN = 0;
        public static final int STARTING_DOWNLOAD = 1;
        public static final int DOWNLOADING = 2;
        public static final int VERIFIED = 3;
    }
}
