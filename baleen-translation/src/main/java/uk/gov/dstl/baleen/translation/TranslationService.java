// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation;

/** Interface for providing different translation services. */
public interface TranslationService {

  /**
   * Translate the given text
   *
   * @param input to be translated
   * @return the translated text
   */
  String translate(String input) throws TranslationException;

  /** @return source language of the translation language */
  String getSourceLanguage();

  /** @return target language of the translation language */
  String getTargetLanguage();
}
