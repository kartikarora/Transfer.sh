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

package me.kartikarora.transfersh.network;

import retrofit.ResponseCallback;
import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.mime.MultipartTypedOutput;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.network
 * Project : Transfer.sh
 * Date : 10/6/16
 */
public class TransferClient {

    private static TransferInterface transferInterface = null;

    public static TransferInterface getInterface(String baseURL) {
        if (transferInterface == null) {
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(baseURL)
                    .build();
            transferInterface = adapter.create(TransferInterface.class);
        }
        return transferInterface;
    }

    public interface TransferInterface {
        @PUT("/{name}")
        void uploadFile(@Body MultipartTypedOutput typedFile, @Path("name") String name, ResponseCallback callback);

        @GET("/")
        void pingServer(ResponseCallback callback);
    }

    public static void nullifyClient() {
        transferInterface = null;
    }
}
