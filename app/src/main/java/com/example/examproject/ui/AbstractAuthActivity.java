package com.example.examproject.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.examproject.ContractApp;
import com.example.examproject.security.ConnectionBuilderForTesting;
import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthState.AuthStateAction;
import net.openid.appauth.AuthorizationService;

public abstract class AbstractAuthActivity extends AppCompatActivity {

  private AuthorizationService authorizationService;

  public AuthorizationService getAuthorizationService() {
    return authorizationService;
  }

  public void performActionWithFreshTokens(AuthStateAction authStateAction) {
    ContractApp.getAuthState().performActionWithFreshTokens(authorizationService, authStateAction);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initAuthorizationService();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (authorizationService == null) {
      initAuthorizationService();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    authorizationService.dispose();
    authorizationService = null;
  }

  private void initAuthorizationService() {
    AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
    builder.setConnectionBuilder(ConnectionBuilderForTesting.INSTANCE);
    authorizationService = new AuthorizationService(this, builder.build());
  }
}
