package uk.gov.dstl.baleen.translation;

import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;

public class TestConfiguredTranslateService implements TranslationService {

  private final String response;

  public TestConfiguredTranslateService(String[] args) {
    response = args[0];
  }

  @Override
  public String translate(String input) throws TranslationException {
    return response;
  }

}
