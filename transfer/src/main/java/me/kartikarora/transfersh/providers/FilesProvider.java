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

package me.kartikarora.transfersh.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import me.kartikarora.transfersh.contracts.FilesContract;
import me.kartikarora.transfersh.helpers.FilesDBHelper;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.providers
 * Project : Transfer.sh
 * Date : 28/6/16
 */
public class FilesProvider extends ContentProvider {

    private SQLiteDatabase mWritableDatabase, mReadableDatabase;

    @Override
    public boolean onCreate() {
        FilesDBHelper mOpenHelper = new FilesDBHelper(getContext());
        mWritableDatabase = mOpenHelper.getWritableDatabase();
        mReadableDatabase = mOpenHelper.getReadableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = mReadableDatabase.query(FilesContract.FilesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return FilesContract.FilesEntry.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri returnUri;
        long _id = mWritableDatabase.insert(FilesContract.FilesEntry.TABLE_NAME, null, values);
        if (_id > 0) {
            returnUri = FilesContract.FilesEntry.buildUri(_id);
            getContext().getContentResolver().notifyChange(FilesContract.BASE_CONTENT_URI, null);
        } else
            throw new android.database.SQLException("Failed to insert row into " + uri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        getContext().getContentResolver().notifyChange(FilesContract.BASE_CONTENT_URI, null);
        return mWritableDatabase.delete(FilesContract.FilesEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        getContext().getContentResolver().notifyChange(FilesContract.BASE_CONTENT_URI, null);
        return mWritableDatabase.update(FilesContract.FilesEntry.TABLE_NAME, values, selection,
                selectionArgs);
    }
}
