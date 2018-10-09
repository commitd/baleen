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
public class JoshuaTranslation {

  private final String translatedText;

  private final List<JoshuaNBest> rawNBest;

  /**
   * Constructor for translation
   *
   * @param translatedText
   * @param rawNBest
   */
  @JsonCreator
  public JoshuaTranslation(
      @JsonProperty("translatedText") String translatedText,
      @JsonProperty("raw_nbest") List<JoshuaNBest> rawNBest) {
    this.translatedText = translatedText;
    this.rawNBest = rawNBest;
  }

  /** @return the translated text */
  public String getTranslatedText() {
    return translatedText;
  }

  /** @return the raw N best */
  public List<JoshuaNBest> getRawNBest() {
    return rawNBest;
  }
}
