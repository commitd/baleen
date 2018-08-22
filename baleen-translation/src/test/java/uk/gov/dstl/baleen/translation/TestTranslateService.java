package uk.gov.dstl.baleen.translation;

import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;

public class TestTranslateService implements TranslationService {

  public static final String DEFAULT_RESPONSE = "Test";

  @Override
  public String translate(String input) throws TranslationException {
    return DEFAULT_RESPONSE;
  }

}
