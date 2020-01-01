package com.example.examproject;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.Log;
import androidx.biometric.BiometricManager;
import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationServiceConfiguration;

public class ContractApp extends Application {

  private static final String TAG = ContractApp.class.getName();

  private static Context context;
  private static AuthorizationServiceConfiguration authorizationServiceConfiguration;
  private static AuthState authState;

  public static Context getAppContext() {
    return context;
  }

  public static AuthorizationServiceConfiguration getAuthorizationServiceConfiguration() {
    return authorizationServiceConfiguration;
  }

  public static AuthState getAuthState() {
    return authState;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    context = getApplicationContext();
    checkBiometricSupport();
    initAuthorizationServiceConfiguration();
    initAuthState();
  }

  private void initAuthorizationServiceConfiguration() {
    String authorizationServerBaseUrl = getString(R.string.authorization_server_base_url);
    String authEndpoint = getString(R.string.auth_endpoint);
    String tokenEndpoint = getString(R.string.token_endpoint);

    authorizationServiceConfiguration =
        new AuthorizationServiceConfiguration(
            Uri.parse(authorizationServerBaseUrl + authEndpoint),
            Uri.parse(authorizationServerBaseUrl + tokenEndpoint));
  }

  private static void initAuthState() {
    // TODO load saved auth state if possible
    authState = new AuthState(authorizationServiceConfiguration);
  }

  private void checkBiometricSupport() {
    BiometricManager biometricManager = BiometricManager.from(this);
    /* TODO show error screen in case of BIOMETRIC_ERROR_NO_HARDWARE or show requirement screen in case of BIOMETRIC_ERROR_HW_UNAVAILABLE and BIOMETRIC_ERROR_NONE_ENROLLED */
    switch (biometricManager.canAuthenticate()) {
      case BiometricManager.BIOMETRIC_SUCCESS:
        Log.d(TAG, "App can authenticate using biometrics.");
        break;
      case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
        throw new IllegalStateException("No biometric features available on this device");
      case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
        throw new IllegalStateException("Biometric features are currently unavailable");
      case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
        throw new IllegalStateException(
            "The user hasn't associated any biometric credentials with their account");
    }
  }

  // Called by the system when the device configuration changes while your component is running.
  // Overriding this method is totally optional!
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  // This is called when the overall system is running low on memory,
  // and would like actively running processes to tighten their belts.
  // Overriding this method is totally optional!
  @Override
  public void onLowMemory() {
    super.onLowMemory();
  }
}
