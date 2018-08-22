package uk.gov.dstl.baleen.translation;

/**
 * Interface for providing different translation services.
 */
public interface TranslationService {


  /**
   * Translate the given text
   *
   * @param input to be translated
   * @return the translated text
   */
  String translate(String input) throws TranslationException;

}
