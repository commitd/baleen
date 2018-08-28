// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation;

public class TestConfiguredTranslateService implements TranslationService {

  public static final String SOURCE_LANGUAGE = "en";
  public static final String TARGET_LANGUAGE = "fr";

  private final String response;

  public TestConfiguredTranslateService(String[] args) {
    response = args[0];
  }

  @Override
  public String translate(String input) throws TranslationException {
    return response;
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
