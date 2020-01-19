package me.kartikarora.transfersh.models

import android.R.attr.mimeType
import android.R.attr.name
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import me.kartikarora.potato.Potato
import me.kartikarora.transfersh.contracts.FilesContract
import me.kartikarora.transfersh.contracts.TransferActivityContract
import me.kartikarora.transfersh.custom.CountingTypedFile
import me.kartikarora.transfersh.custom.CountingTypedFile.FileUploadListener
import me.kartikarora.transfersh.helpers.UtilsHelper
import me.kartikarora.transfersh.network.NetworkResponseListener
import me.kartikarora.transfersh.network.TransferClient
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import retrofit.ResponseCallback
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit.mime.MultipartTypedOutput
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class TransferActivityModel(private val context: Context) : TransferActivityContract.Model {

    override fun pingServerForResponse(serverUrl: String, listener: NetworkResponseListener) {

        TransferClient.getInterface(serverUrl).pingServer(object : ResponseCallback() {
            override fun success(response: Response) {
                listener.success(response)
            }

            override fun failure(error: RetrofitError) {
                error.printStackTrace()
                listener.failure(error)
            }
        })
    }

    override fun getFileFromContentResolverUsingUri(uri: Uri): File {
        lateinit var file: File
        val cursor: Cursor? = this.context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val name = cursor.getString(nameIndex)
            try {
                val inputStream: InputStream? = this.context.contentResolver.openInputStream(uri)
                val outputStream: OutputStream = this.context.openFileOutput(name, Context.MODE_PRIVATE)
                if (inputStream != null) {
                    IOUtils.copy(inputStream, outputStream)
                    file = File(this.context.filesDir, name)
                }
                cursor.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    override fun getMimeTypeOfFileUsingUriFromContentResolver(uri: Uri): String {
        return this.context.contentResolver.getType(uri)!!
    }

    override fun createMultipartDataFromFileToUpload(fileToUpload: File, mimeType: String,
                                                     fileUploadListener: FileUploadListener): MultipartTypedOutput {
        val multipartTypedOutput = MultipartTypedOutput()
        multipartTypedOutput.addPart(fileToUpload.name, CountingTypedFile(mimeType, fileToUpload, fileUploadListener))
        return multipartTypedOutput
    }

    override fun uploadMultipartTypedOutputToRemoteServer(baseUrl: String, name: String,
                                                          multipartTypedOutput: MultipartTypedOutput,
                                                          listener: NetworkResponseListener) {
        TransferClient.getInterface(baseUrl).uploadFile(multipartTypedOutput, name,
                object : ResponseCallback() {
                    override fun success(response: Response) {
                        listener.success(response)
                    }

                    override fun failure(error: RetrofitError) {
                        listener.failure(error)
                    }
                }
        )
    }

    override fun storeStringInPreference(key: String, value: String) {
        Potato.potate(context).Preferences().putSharedPreference(key, value)
    }

    override fun getStringFromPreference(key: String): String {
        return Potato.potate(context).Preferences().getSharedPreferenceString(key)
    }

    override fun storeBooleanInPreference(key: String, value: Boolean) {
        Potato.potate(context).Preferences().putSharedPreference(key, value)
    }

    override fun getBooleanFromPreference(key: String): Boolean {
        return Potato.potate(context).Preferences().getSharedPreferenceBoolean(key)
    }

    override fun getCursorLoader(): Loader<Cursor> {
        return CursorLoader(this.context, FilesContract.BASE_CONTENT_URI, null, null, null, null)
    }

    override fun saveUploadedFileMetaToDatabase(uploadedFile: File, shareableUrl: String, uriInContentResolver: Uri) {
        val sdf: SimpleDateFormat = UtilsHelper.getInstance().sdf
        val upCal: Calendar = Calendar.getInstance()
        upCal.time = Date(uploadedFile.lastModified())
        val delCal: Calendar = Calendar.getInstance()
        delCal.time = upCal.time
        delCal.add(Calendar.DATE, 14)
        val values = ContentValues()
        values.put(FilesContract.FilesEntry.COLUMN_NAME, name)
        values.put(FilesContract.FilesEntry.COLUMN_TYPE, mimeType)
        values.put(FilesContract.FilesEntry.COLUMN_URL, shareableUrl)
        values.put(FilesContract.FilesEntry.COLUMN_URI, uriInContentResolver.toString())
        values.put(FilesContract.FilesEntry.COLUMN_SIZE, java.lang.String.valueOf(uploadedFile.totalSpace))
        values.put(FilesContract.FilesEntry.COLUMN_DATE_UPLOAD, sdf.format(upCal.time))
        values.put(FilesContract.FilesEntry.COLUMN_DATE_DELETE, sdf.format(delCal.time))
        this.context.contentResolver.insert(FilesContract.BASE_CONTENT_URI, values)
        FileUtils.deleteQuietly(uploadedFile)
    }

    override fun fireIntent(intent: Intent) {
        this.context.startActivity(intent)
    }

    override fun getUriFromFileIdToReuploadFile(id: Long): Uri {
        lateinit var uri: Uri
        val cursor: Cursor? = this.context.contentResolver.query(FilesContract.BASE_CONTENT_URI, null, FilesContract.FilesEntry._ID + "=?", arrayOf(id.toString()), null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                uri = Uri.parse(this.context.getString(cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_URI)))
                this.context.contentResolver.delete(FilesContract.BASE_CONTENT_URI, FilesContract.FilesEntry._ID + "=?", arrayOf(id.toString()))
            }
            cursor.close()
        }
        return uri
    }
}
