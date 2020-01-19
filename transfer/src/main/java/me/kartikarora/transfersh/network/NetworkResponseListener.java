package me.kartikarora.transfersh.network;

import android.net.Uri;

import java.io.File;

import retrofit.RetrofitError;
import retrofit.client.Response;

public interface NetworkResponseListener {
    public void onSuccess(String url);

    public void onFailure(Response response);
}
