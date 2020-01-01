package com.example.examproject.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public final class SignatureModel implements Serializable {

  private static final long serialVersionUID = 1L;
  private final String base64Signature;

  @JsonCreator
  public SignatureModel(@JsonProperty("base64Signature") String base64Signature) {
    this.base64Signature = base64Signature;
  }

  public String getBase64Signature() {
    return base64Signature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignatureModel that = (SignatureModel) o;
    return Objects.equals(base64Signature, that.base64Signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(base64Signature);
  }

  @Override
  public String toString() {
    return "SignatureModel{" + "base64Signature='" + base64Signature + '\'' + '}';
  }
}
