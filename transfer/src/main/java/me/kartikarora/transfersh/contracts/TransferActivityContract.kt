package me.kartikarora.transfersh.contracts

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.google.android.gms.ads.AdRequest
import me.kartikarora.transfersh.network.NetworkResponseListener
import retrofit.mime.MultipartTypedOutput
import java.io.File

class TransferActivityContract {

    interface View {
        fun initView()
        fun loadDataFromLoader()
        fun initGrid(data: Cursor?)
        fun resetGrid()
        fun getContextFromView(): Context
        fun showPleaseWaitDialog()
        fun hidePleaseWaitDialog()
        fun showServerUrlChangeAlertDialog(serverUrl: String)
        fun hideServerUrlChangeAlertDialog()
        fun showSnackbar(text: String)
        fun showSnackbar(textResource: Int)
        fun showSnackbarWithAction(name: String, shareableUrl: String)
        fun setBottomSheetState(state: Int)
        fun setUploadingFileNameInBottomSheet(text: String?)
        fun setUploadProgressPercentage(percentage: Int)
        fun loadAdRequest(adRequest: AdRequest)

    }

    interface Model {
        fun pingServerForResponse(serverUrl: String, listener: NetworkResponseListener)
        fun getFileFromUri(uri: Uri): File
        fun getMimeTypeOfFileFromUri(uri: Uri): String
        fun getMultipartDataFromFile(fileToUpload: File, mimeType: String): MultipartTypedOutput
        fun uploadMultipartTypedOutputToRemoteServer(baseUrl: String, name: String,
                                                     multipartTypedOutput: MultipartTypedOutput,
                                                     listener: NetworkResponseListener)

        fun storeItemInPreference(key: String, value: String)
        fun getItemFromPreference(key: String): String
        fun getLoader(): Loader<Cursor>
    }

    interface Presenter {
        fun requestDataFromLoader()
        fun checkIfServerIsResponsive(serverUrl: String)
        fun initiateFileUploadFromUri(uri: Uri)
        fun initiateServerUrlChange()
        fun buildAdRequest(): AdRequest
        fun getLoaderCallbacks(): LoaderManager.LoaderCallbacks<Cursor>
    }
}