package me.kartikarora.transfersh.network

import retrofit.RetrofitError
import retrofit.client.Response

interface NetworkResponseListener {
    fun success(response: Response)
    fun failure(error: RetrofitError)
}