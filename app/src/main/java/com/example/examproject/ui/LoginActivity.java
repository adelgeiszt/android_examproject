package com.example.examproject.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.example.examproject.ContractApp;
import com.example.examproject.MainActivity;
import com.example.examproject.R;
import com.example.examproject.security.RsaSupport;
import java.util.concurrent.CompletableFuture;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.ResponseTypeValues;

public class LoginActivity extends AbstractAuthActivity implements TextWatcher {

  private static final String TAG = LoginActivity.class.getName();
  private static final int RC_AUTH = 0;

  private EditText username;
  private EditText password;
  private Button login;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    username = findViewById(R.id.username);
    password = findViewById(R.id.password);
    login = findViewById(R.id.login);

    login.setOnClickListener(this::onSignInClicked);
    username.addTextChangedListener(this);
    password.addTextChangedListener(this);
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    // No-op.
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (username.getText().length() > 0 && password.getText().length() > 0) {
      login.setEnabled(true);

    } else {
      login.setEnabled(false);
    }
  }

  @Override
  public void afterTextChanged(Editable s) {
    // No-op.
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_AUTH) {
      AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
      AuthorizationException ex = AuthorizationException.fromIntent(data);
      ContractApp.getAuthState().update(resp, ex);

      if (ex != null) {
        Log.e(TAG, "Authorization failed", ex);
      } else {
        Log.i(TAG, "Authorization successfully done: " + resp.authorizationCode);
        getAuthorizationService()
            .performTokenRequest(
                resp.createTokenExchangeRequest(),
                (tokenResponse, error) -> {
                  ContractApp.getAuthState().update(tokenResponse, error);

                  if (error != null || tokenResponse == null) {
                    // exchange succeeded
                    Log.e(TAG, "Access token acquisition failed", error);
                  } else {
                    Log.i(
                        TAG,
                        String.format(
                            "Access token acquisition succeeded - accessToken: [%s]",
                            tokenResponse.accessToken));
                    onAuthSuccess();
                  }
                });
      }
    }
  }

  private void onSignInClicked(View view) {
    AuthorizationRequest.Builder authRequestBuilder =
        new AuthorizationRequest.Builder(
            ContractApp.getAuthorizationServiceConfiguration(),
            getString(R.string.oauth2_client),
            ResponseTypeValues.CODE,
            Uri.parse(getString(R.string.oatuh2_redirect_uri)));

    AuthorizationRequest authorizationRequest =
        authRequestBuilder.setScope("openid").setPrompt("login").build();

    Intent authIntent =
        getAuthorizationService().getAuthorizationRequestIntent(authorizationRequest);
    startActivityForResult(authIntent, RC_AUTH);
  }

  private void onAuthSuccess() {
    if (!RsaSupport.isKeyPairGenerated()) {
      CompletableFuture.runAsync(RsaSupport::generateRsaKeyPair)
          .thenRun(() -> Log.i(TAG, "New key pair has been generated"));
    }

    Intent intent = new Intent(this, MainActivity.class);
    ActivityOptions options =
        ActivityOptions.makeCustomAnimation(this, R.anim.entry_anim, R.anim.exit_anim);
    startActivity(intent, options.toBundle());
  }
}
