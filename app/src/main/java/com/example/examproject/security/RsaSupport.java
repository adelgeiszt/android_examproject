package com.example.examproject.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.StrongBoxUnavailableException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

public final class RsaSupport {

  private static final String KEY_STORE_NAME = "AndroidKeyStore";
  private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
  private static final int KEY_SIZE = 4096;

  public static final String KEY_ALIAS = "DocumentServiceRsaKeys";

  private RsaSupport() {
    // Private constructor for singleton
  }

  public static KeyPair generateRsaKeyPair() {
    try {
      KeyPairGenerator kpg =
          KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_NAME);

      kpg.initialize(
          new KeyGenParameterSpec.Builder(
                  KEY_ALIAS,
                  KeyProperties.PURPOSE_SIGN
                      | KeyProperties.PURPOSE_VERIFY
                      | KeyProperties.PURPOSE_ENCRYPT
                      | KeyProperties.PURPOSE_DECRYPT)
              .setDigests(KeyProperties.DIGEST_SHA256)
              .setKeySize(KEY_SIZE)
              .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
              // .setIsStrongBoxBacked(true) TODO use this configuration for physical devices
              .setRandomizedEncryptionRequired(true)
              .setUnlockedDeviceRequired(true)
              // .setUserConfirmationRequired(true)
              // I think the biometric prompt is a sufficient confirmation
              .setUserPresenceRequired(true)
              .setUserAuthenticationRequired(true)
              .setUserAuthenticationValidityDurationSeconds(3)
              .setInvalidatedByBiometricEnrollment(true)
              .build(),
          SecureRandom.getInstanceStrong());

      return kpg.generateKeyPair();
    } catch (StrongBoxUnavailableException e) {
      // TODO show error screen if no secure hardware is present on the device
      throw new RsaException("No secure hardware is present on the device", e);
    } catch (NoSuchAlgorithmException
        | NoSuchProviderException
        | InvalidAlgorithmParameterException e) {
      throw new RsaException("Failed to generate RSA key pair", e);
    }
  }

  public static Signature getSignatureInstanceForSignatory(
      PrivateKey privateKey, byte[] dataToSign) {
    try {
      Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
      signature.initSign(privateKey);
      signature.update(dataToSign);
      return signature;
    } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
      throw new RsaException("Failed to init Signature instance", e);
    }
  }

  public static PublicKey getPublicKey() {
    try {
      KeyStore keyStore = getKeyStore();
      return keyStore.getCertificate(KEY_ALIAS).getPublicKey();
    } catch (KeyStoreException e) {
      throw new RsaException("Failed to get public key", e);
    }
  }

  public static PrivateKey getPrivateKey() {
    try {
      KeyStore keyStore = getKeyStore();
      KeyStore.Entry entry = keyStore.getEntry(KEY_ALIAS, null);
      return ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
      throw new RsaException("Failed to get private key", e);
    }
  }

  public static boolean isKeyPairGenerated() {
    try {
      return getKeyStore().containsAlias(KEY_ALIAS);
    } catch (KeyStoreException e) {
      throw new RsaException("Failed to verify key pair existence", e);
    }
  }

  private static KeyStore getKeyStore() {
    try {
      KeyStore keyStore = KeyStore.getInstance(KEY_STORE_NAME);
      keyStore.load(null);
      return keyStore;
    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      throw new RsaException("Failed to load key store", e);
    }
  }
}
