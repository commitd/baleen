// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wrapping object for the Joshua response
 *
 * @see https://cwiki.apache.org/confluence/display/JOSHUA/RESTful+API
 */
public class JoshuaResponse {

  private final JoshuaTranslations data;

  private final List<String> metadata;

  /**
   * Construct a JoshuaResponse
   *
   * @param data
   * @param metadata
   */
  @JsonCreator
  public JoshuaResponse(
      @JsonProperty("data") JoshuaTranslations data,
      @JsonProperty("metadata") List<String> metadata) {
    this.data = data;
    this.metadata = metadata;
  }

  /** @return the data */
  public JoshuaTranslations getData() {
    return data;
  }

  /** @return the metadata */
  public List<String> getMetadata() {
    return metadata;
  }
}
