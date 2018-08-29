// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link JoshuaResponse}
 *
 * @see https://cwiki.apache.org/confluence/display/JOSHUA/RESTful+API
 */
public class JoshuaTranslations {

  private final List<JoshuaTranslation> translations;

  /**
   * Constructor for translations
   *
   * @param translations
   */
  @JsonCreator
  public JoshuaTranslations(@JsonProperty("translations") List<JoshuaTranslation> translations) {
    this.translations = translations;
  }

  /** @return the translations */
  public List<JoshuaTranslation> getTranslations() {
    return translations;
  }
}
