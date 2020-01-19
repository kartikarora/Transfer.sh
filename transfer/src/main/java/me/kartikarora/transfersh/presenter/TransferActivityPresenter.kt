package me.kartikarora.transfersh.presenter

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import me.kartikarora.transfersh.R
import me.kartikarora.transfersh.activities.SplashActivity.PREF_URL_FLAG
import me.kartikarora.transfersh.contracts.TransferActivityContract
import me.kartikarora.transfersh.models.TransferActivityModel
import me.kartikarora.transfersh.network.NetworkResponseListener
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


public class TransferActivityPresenter(view: TransferActivityContract.View)
    : TransferActivityContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {
    private val mModel: TransferActivityContract.Model
    private val mView: TransferActivityContract.View = view

    init {
        mModel = TransferActivityModel(mView.getContextFromView())
        mView.initView()
    }

    override fun requestDataFromLoader() {
        mView.loadDataFromLoader()
    }

    override fun checkIfServerIsResponsive(serverUrl: String) {
        mView.showPleaseWaitDialog()
        mModel.pingServerForResponse(serverUrl, object : NetworkResponseListener {
            override fun success(response: Response) {
                if (response.status == 200) {
                    val headerList = response.headers
                    for (header in headerList) {
                        if (!TextUtils.isEmpty(header.name) && header.name.equals("server", ignoreCase = true)) {
                            val value = header.value
                            if (value.toLowerCase().contains("transfer.sh")) {
                                mModel.storeItemInPreference(PREF_URL_FLAG, serverUrl)
                                mView.hidePleaseWaitDialog()
                            } else {
                                mView.hidePleaseWaitDialog()
                                mView.showServerUrlChangeAlertDialog(serverUrl)
                            }
                        }
                    }
                }
            }

            override fun failure(error: RetrofitError) {
                mView.hidePleaseWaitDialog()
                mView.showServerUrlChangeAlertDialog(serverUrl)
            }
        })
    }

    override fun initiateFileUploadFromUri(uri: Uri) {
        val baseUrl = mModel.getItemFromPreference((PREF_URL_FLAG))
        val fileToUpload: File = mModel.getFileFromUri(uri)
        val name = fileToUpload.name
        mView.setUploadingFileNameInBottomSheet(name)
        val mimeType: String = mModel.getMimeTypeOfFileFromUri(uri)
        val multipartTypedOutput = mModel.getMultipartDataFromFile(fileToUpload, mimeType)
        mView.setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        mModel.uploadMultipartTypedOutputToRemoteServer(baseUrl, name, multipartTypedOutput,
                object : NetworkResponseListener {
                    override fun success(response: Response) {
                        val reader: BufferedReader
                        val sb = StringBuilder()
                        try {
                            reader = BufferedReader(InputStreamReader(response.body.`in`()))
                            var line: String?
                            try {
                                while (reader.readLine().also { line = it } != null) {
                                    sb.append(line)
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val result = sb.toString()
                        mView.showSnackbarWithAction(name, result)
                    }

                    override fun failure(error: RetrofitError) {
                        error.printStackTrace()
                        mView.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                        mView.showSnackbar(R.string.something_went_wrong)
                    }
                })
    }


    override fun initiateServerUrlChange() {
        val serverURL = mModel.getItemFromPreference(PREF_URL_FLAG)
        mView.showServerUrlChangeAlertDialog(serverURL)
    }

    override fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return mModel.getLoader()
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mView.initGrid(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mView.resetGrid()
    }

    override fun getLoaderCallbacks(): LoaderManager.LoaderCallbacks<Cursor> {
        return this
    }
}