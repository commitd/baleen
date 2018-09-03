// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;

/** Configuration details for the {@link JoshuaTranslationService}. */
public class JoshuaConfiguration {

  private final String url;
  private final String source;
  private final String target;

  /**
   * Constructor
   *
   * @param url full Joshua url, including port
   * @param source language
   * @param target language
   */
  public JoshuaConfiguration(String url, String source, String target) {
    this.url = url;
    this.source = source;
    this.target = target;
  }

  /** @return the url */
  public String getUrl() {
    return url;
  }

  /** @return the source */
  public String getSource() {
    return source;
  }

  /** @return the target */
  public String getTarget() {
    return target;
  }

  /**
   * Factory method to create {@link JoshuaConfiguration} from configuration strings
   *
   * @param configuration list of strings for configuration, see {@link JoshuaTranslationService}
   * @return JoshuaConfiguration
   * @throws ResourceInitializationException if unable to create configuration
   */
  public static JoshuaConfiguration create(Map<String, Object> configuration)
      throws ResourceInitializationException {
    boolean hasSource = configuration.containsKey(JoshuaTranslationService.SOURCE);
    boolean hasTarget = configuration.containsKey(JoshuaTranslationService.TARGET);

    if (!hasSource || !hasTarget) {
      throw new ResourceInitializationException();
    }

    String source;
    String target;
    String url;

    try {
      source = (String) configuration.get(JoshuaTranslationService.SOURCE);
      target = (String) configuration.get(JoshuaTranslationService.TARGET);
      url =
          (String)
              configuration.getOrDefault(
                  JoshuaTranslationService.URL, JoshuaTranslationService.DEFAULT_URL);
    } catch (ClassCastException e) {
      throw new ResourceInitializationException(e);
    }

    return new JoshuaConfiguration(url, source, target);
  }
}
