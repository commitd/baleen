// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import java.util.List;

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
  public static JoshuaConfiguration create(List<String> configuration)
      throws ResourceInitializationException {
    int indexOfSource = configuration.indexOf(JoshuaTranslationService.SOURCE);
    int indexOfTarget = configuration.indexOf(JoshuaTranslationService.TARGET);
    int indexOfUrl = configuration.indexOf(JoshuaTranslationService.URL);

    if (indexOfSource < 0 || indexOfTarget < 0) {
      throw new ResourceInitializationException();
    }

    String source;
    String target;
    String url;

    try {
      source = configuration.get(indexOfSource + 1);
      target = configuration.get(indexOfTarget + 1);
      if (indexOfUrl < 0) {
        url = JoshuaTranslationService.DEFAULT_URL;
      } else {
        url = configuration.get(indexOfUrl + 1);
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new ResourceInitializationException(e);
    }

    return new JoshuaConfiguration(url, source, target);
  }
}
