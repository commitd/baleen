// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link JoshuaResponse}
 *
 * @see https://cwiki.apache.org/confluence/display/JOSHUA/RESTful+API
 */
public class JoshuaNBest {

  private final String hyp;

  private final double totalScore;

  /**
   * Construct a JoshuaNBest
   *
   * @param hyp
   * @param totalScore
   */
  @JsonCreator
  public JoshuaNBest(
      @JsonProperty("hyp") String hyp, @JsonProperty("totalScore") double totalScore) {
    this.hyp = hyp;
    this.totalScore = totalScore;
  }

  /** @return the hyp */
  public String getHyp() {
    return hyp;
  }

  /** @return the total score */
  public double getTotalScore() {
    return totalScore;
  }
}
