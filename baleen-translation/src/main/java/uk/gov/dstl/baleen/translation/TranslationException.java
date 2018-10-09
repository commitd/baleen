// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation;

/** General exception for errors during translation */
public class TranslationException extends Exception {

  /** Generated */
  private static final long serialVersionUID = -6523697794008835216L;

  /**
   * Constructor
   *
   * @see Exception#Exception()
   */
  public TranslationException() {}

  /**
   * Constructor
   *
   * @param message
   * @see Exception#Exception(String)
   */
  public TranslationException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param cause
   * @see Exception#Exception(Throwable)
   */
  public TranslationException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor
   *
   * @param message
   * @param cause
   * @see Exception#Exception(String, Throwable)
   */
  public TranslationException(String message, Throwable cause) {
    super(message, cause);
  }
}
