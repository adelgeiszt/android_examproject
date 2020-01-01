package com.example.examproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.AuthenticationCallback;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.examproject.security.RsaSupport;
import com.example.examproject.service.Document;
import com.example.examproject.service.RetrofitService;
import com.example.examproject.service.SignatureModel;
import com.example.examproject.ui.AbstractAuthActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.Executor;
import net.openid.appauth.AuthState.AuthStateAction;
import net.openid.appauth.AuthorizationException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewActivity extends AbstractAuthActivity {

  public static final String DOCUMENT_ID = "documentId";

  private static final String TAG = WebViewActivity.class.getName();

  @BindView(R.id.web_view)
  WebView webView;

  @BindView(R.id.acceptBtn)
  FloatingActionButton acceptBtn;

  private Document documentInfo;
  private byte[] dataToSign;
  private String document;
  private Executor executor;
  private BiometricPrompt biometricPrompt;
  private BiometricPrompt.PromptInfo biometricPromptInfo;

  private int activityResult = Activity.RESULT_CANCELED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_layout);
    ButterKnife.bind(this);

    documentInfo =
        Optional.ofNullable(getIntent().getExtras())
            .map(bundle -> bundle.getSerializable(DOCUMENT_ID))
            .map(Document.class::cast)
            .orElse(null);
    Log.i(TAG, String.format("Document: [%s]", documentInfo));

    webView.setWebViewClient(new WebViewClient());

    if (documentInfo != null) {
      downloadDocument(documentInfo.getDocumentId());
    }

    executor = ContextCompat.getMainExecutor(this);

    initBiometricPrompt();

    acceptBtn.setOnClickListener(this::onSignButtonClicked);
  }

  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();

    } else {
      super.onBackPressed();
      Intent returnIntent = new Intent();
      setResult(activityResult, returnIntent);
      finish();
      overridePendingTransition(R.anim.entry_anim, R.anim.exit_anim);
    }
  }

  private void downloadDocument(String documentId) {
    performActionWithFreshTokens(
        new AuthStateAction() {
          @Override
          public void execute(
              @Nullable String accessToken,
              @Nullable String idToken,
              @Nullable AuthorizationException ex) {
            RetrofitService.getInstance()
                .getDocument(documentId, accessToken)
                .enqueue(
                    new Callback<ResponseBody>() {
                      @Override
                      public void onResponse(
                          Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                          byte[] responseBodyBytes =
                              response.body() != null ? response.body().bytes() : null;
                          String responseBody =
                              responseBodyBytes != null
                                  ? new String(responseBodyBytes, StandardCharsets.UTF_8)
                                  : null;

                          if (response.isSuccessful() && responseBody != null) {
                            dataToSign = responseBodyBytes;
                            document = responseBody;
                            webView.loadData(document, "text/html", StandardCharsets.UTF_8.name());
                          } else {
                            Log.e(
                                WebViewActivity.TAG,
                                String.format(
                                    "Failed to load document [%s] - statusCode: [%d], responseBody: [%s]",
                                    documentId, response.code(), responseBody));
                          }
                        } catch (IOException e) {
                          Log.e(
                              WebViewActivity.TAG,
                              String.format(
                                  "Failed to get response body of document [%s]", documentId),
                              e);
                        }
                      }

                      @Override
                      public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(
                            WebViewActivity.TAG,
                            String.format("Failed to load document [%s]", documentId),
                            t);
                      }
                    });
          }
        });
  }

  private void initBiometricPrompt() {
    biometricPrompt =
        new BiometricPrompt(this, executor, new DigitalSignatureAuthenticationCallback());
    biometricPromptInfo =
        new BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_signature_access_title))
            .setSubtitle(getString(R.string.biometric_signature_access_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_signature_access_navigate_btn_txt))
            .build();
  }

  private void onSignButtonClicked(View view) {
    if (dataToSign == null || dataToSign.length == 0) {
      Toast.makeText(getApplicationContext(), "Document is not available", Toast.LENGTH_SHORT)
          .show();
    } else {
      biometricPrompt.authenticate(biometricPromptInfo);
    }
  }

  private final class DigitalSignatureAuthenticationCallback extends AuthenticationCallback {
    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
      super.onAuthenticationError(errorCode, errString);
      Toast.makeText(
              getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT)
          .show();
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
      super.onAuthenticationSucceeded(result);
      String message = "Document cannot be signed";
      try {
        Signature signature =
            RsaSupport.getSignatureInstanceForSignatory(RsaSupport.getPrivateKey(), dataToSign);
        String base64Signature = Base64.getEncoder().encodeToString(signature.sign());
        SignatureModel signatureModel = new SignatureModel(base64Signature);

        Log.i(
            WebViewActivity.TAG,
            String.format("Signature successfully created: [%s]", base64Signature));
        message = "Document successfully signed";

        performActionWithFreshTokens(
            new AuthStateAction() {
              @Override
              public void execute(
                  @Nullable String accessToken,
                  @Nullable String idToken,
                  @Nullable AuthorizationException ex) {
                RetrofitService.getInstance()
                    .postSignature(documentInfo.getDocumentId(), signatureModel, accessToken)
                    .enqueue(
                        new Callback<ResponseBody>() {
                          @Override
                          public void onResponse(
                              Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                              acceptBtn.hide();
                              Log.i(
                                  WebViewActivity.TAG,
                                  String.format(
                                      "Signature successfully posted for document [%s]",
                                      documentInfo.getDocumentId()));

                            } else {
                              try {
                                String errorResponseBody =
                                    response.errorBody() == null
                                        ? null
                                        : new String(
                                            response.errorBody().bytes(), StandardCharsets.UTF_8);

                                Log.e(
                                    WebViewActivity.TAG,
                                    String.format(
                                        "Failed to post signature of document [%s] - statusCode: [%d], responseBody: [%s]",
                                        documentInfo.getDocumentId(),
                                        response.code(),
                                        errorResponseBody));
                              } catch (IOException e) {
                                Log.e(WebViewActivity.TAG, "Failed to parse response body", e);
                              }
                            }
                          }

                          @Override
                          public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(
                                WebViewActivity.TAG,
                                String.format(
                                    "Signature post failed for document [%s]",
                                    documentInfo.getDocumentId()));
                          }
                        });
              }
            });
        activityResult = Activity.RESULT_OK;
      } catch (SignatureException e) {
        Log.e(WebViewActivity.TAG, "Failed to initialize signature", e);
        message = "Unexpected signature error: " + e.getMessage();
      } finally {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
      }
    }

    @Override
    public void onAuthenticationFailed() {
      super.onAuthenticationFailed();
      Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
    }
  }
}
