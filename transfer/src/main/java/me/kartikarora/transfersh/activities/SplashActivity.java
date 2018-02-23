package me.kartikarora.transfersh.activities;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.List;

import me.kartikarora.potato.Potato;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.network.TransferClient;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by kartik
 * Project Name: Transfer.sh
 * Package: me.kartikarora.transfersh.activities
 * <p>
 * Update History:
 * Created on 23 Feb, 2018 at 11:16 PM by kartik
 */

public class SplashActivity extends AppCompatActivity {
    public static final String PREF_URL_FLAG = "urlFlag";
    private String serverURL;
    private FloatingActionButton readyButton;
    private ProgressBar setupProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        readyButton = findViewById(R.id.ready_button);
        setupProgressBar = findViewById(R.id.setup_progress_bar);

        serverURL = Potato.potate(SplashActivity.this).Preferences().getSharedPreferenceString(PREF_URL_FLAG);
        if (TextUtils.isEmpty(serverURL)) {
            serverURL = "https://transfer.sh";
            setServerUrl(getString(R.string.setup_url), getString(R.string.setup_url_message, getString(android.R.string.cancel)));
        } else {
            beginCheck();
        }
    }

    private void beginCheck() {
        TransferClient.nullifyClient();
        TransferClient.getInterface(serverURL).pingServer(new ResponseCallback() {
            @Override
            public void success(Response response) {
                if (response.getStatus() == 200) {

                    List<Header> headerList = response.getHeaders();
                    for (Header header : headerList) {
                        if (!TextUtils.isEmpty(header.getName()) && header.getName().equalsIgnoreCase("server")) {
                            String value = header.getValue();
                            if (value.toLowerCase().contains("transfer.sh")) {
                                Potato.potate(SplashActivity.this).Preferences().putSharedPreference(PREF_URL_FLAG, serverURL);


                                if (setupProgressBar.isShown()) {
                                    setupProgressBar.setVisibility(View.GONE);
                                    readyButton.setVisibility(View.VISIBLE);
                                }

                                if (readyButton.isShown()) {
                                    readyButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ActivityOptions options = null;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                options = ActivityOptions.makeSceneTransitionAnimation(
                                                        SplashActivity.this,
                                                        android.util.Pair.create((View) readyButton, "coordinator_layout"));
                                            }
                                            startActivity(new Intent(SplashActivity.this, TransferActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK), options.toBundle());
                                        }
                                    });
                                } else {
                                    ActivityOptions options = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        options = ActivityOptions.makeSceneTransitionAnimation(
                                                SplashActivity.this,
                                                android.util.Pair.create((View) readyButton, "coordinator_layout"));
                                    }
                                    startActivity(new Intent(SplashActivity.this, TransferActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK), options.toBundle());
                                }
                            } else {
                                setServerUrl(getString(R.string.server_error), getString(R.string.server_error_message, serverURL));
                            }
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
                setServerUrl(getString(R.string.server_error), getString(R.string.server_error_message, serverURL));
            }
        });
    }

    private void setServerUrl(String title, String message) {
        setupProgressBar.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint(serverURL);
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                serverURL = input.getText().toString();
                if (TextUtils.isEmpty(serverURL)) {
                    serverURL = "https://transfer.sh";
                }
                beginCheck();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                beginCheck();
            }
        });
        builder.show();
    }
}
