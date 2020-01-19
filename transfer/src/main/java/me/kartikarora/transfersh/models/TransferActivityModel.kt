package me.kartikarora.transfersh.models

import android.content.Context
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
import me.kartikarora.transfersh.network.NetworkResponseListener
import me.kartikarora.transfersh.network.TransferClient
import org.apache.commons.io.IOUtils
import retrofit.ResponseCallback
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit.mime.MultipartTypedOutput
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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

    override fun getFileFromUri(uri: Uri): File {
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

    override fun getMimeTypeOfFileFromUri(uri: Uri): String {
        return this.context.contentResolver.getType(uri)!!
    }

    override fun getMultipartDataFromFile(fileToUpload: File, mimeType: String): MultipartTypedOutput {
        val multipartTypedOutput = MultipartTypedOutput()
        multipartTypedOutput.addPart(fileToUpload.name, CountingTypedFile(mimeType, fileToUpload, FileUploadListener { num ->
            val per = Math.round(num / fileToUpload.length().toFloat() * 100)

        }))
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

    override fun storeItemInPreference(key: String, value: String) {
        Potato.potate(context).Preferences().putSharedPreference(key, value)
    }

    override fun getItemFromPreference(key: String): String {
        return Potato.potate(context).Preferences().getSharedPreferenceString(key)
    }

    override fun getLoader(): Loader<Cursor> {
        return CursorLoader(this.context, FilesContract.BASE_CONTENT_URI, null, null, null, null)
    }


}