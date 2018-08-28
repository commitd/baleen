// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation;

public class TestTranslateService implements TranslationService {

  public static final String SOURCE_LANGUAGE = "en";
  public static final String TARGET_LANGUAGE = "fr";
  public static final String DEFAULT_RESPONSE = "Test";

  @Override
  public String translate(String input) throws TranslationException {
    return DEFAULT_RESPONSE;
  }

  @Override
  public String getSourceLanguage() {
    return SOURCE_LANGUAGE;
  }

  @Override
  public String getTargetLanguage() {
    return TARGET_LANGUAGE;
  }
}
