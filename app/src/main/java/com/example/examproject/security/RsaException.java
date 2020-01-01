package com.example.examproject.security;

public final class RsaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RsaException(String message) {
    super(message);
  }

  public RsaException(String message, Throwable cause) {
    super(message, cause);
  }
}
