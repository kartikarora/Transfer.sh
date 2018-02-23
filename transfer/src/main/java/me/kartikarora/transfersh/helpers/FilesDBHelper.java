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

package me.kartikarora.transfersh.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.kartikarora.transfersh.contracts.FilesContract;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.helpers
 * Project : Transfer.sh
 * Date : 28/6/16
 */

public class FilesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "files.db";

    public FilesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_TABLE = "CREATE TABLE " + FilesContract.FilesEntry.TABLE_NAME + " (" +
                FilesContract.FilesEntry._ID + " INTEGER PRIMARY KEY, " +
                FilesContract.FilesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_URL + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_URI + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_SIZE + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_DATE_UPLOAD + " TEXT NOT NULL, " +
                FilesContract.FilesEntry.COLUMN_DATE_DELETE + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FilesContract.FilesEntry.TABLE_NAME);
        onCreate(db);
    }

}
