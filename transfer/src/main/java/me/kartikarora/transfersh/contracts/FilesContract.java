/**
 * Copyright 2018 Kartik Arora
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.kartikarora.transfersh.contracts;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import me.kartikarora.transfersh.BuildConfig;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.contracts
 * Project : Transfer.sh
 * Date : 28/6/16
 */
public class FilesContract {

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + BuildConfig.APPLICATION_ID);

    public static final class FilesEntry implements BaseColumns {
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + BuildConfig.APPLICATION_ID;

        public static final String TABLE_NAME = "filesdata";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_DATE_UPLOAD = "date_up";
        public static final String COLUMN_DATE_DELETE = "date_del";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(BASE_CONTENT_URI, id);
        }

    }
}
