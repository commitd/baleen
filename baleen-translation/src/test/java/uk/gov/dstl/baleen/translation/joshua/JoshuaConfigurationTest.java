// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.translation.joshua;

import static org.junit.Assert.assertEquals;

import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class JoshuaConfigurationTest {

  private static final String SOURCE = "fr";
  private static final String TARGET = "en";
  private static final String URL = "http://url:123";

  @Test(expected = ResourceInitializationException.class)
  public void testRequiresConfig() throws ResourceInitializationException {
    JoshuaConfiguration.create(ImmutableList.of());
  }

  @Test(expected = ResourceInitializationException.class)
  public void testRequiresSource() throws ResourceInitializationException {
    JoshuaConfiguration.create(
        ImmutableList.of(
            JoshuaTranslationService.TARGET, TARGET, JoshuaTranslationService.URL, URL));
  }

  @Test(expected = ResourceInitializationException.class)
  public void testRequiresTarget() throws ResourceInitializationException {
    JoshuaConfiguration.create(
        ImmutableList.of(
            JoshuaTranslationService.SOURCE, SOURCE, JoshuaTranslationService.URL, URL));
  }

  @Test
  public void testRequiresOnlySourceAndTarget() throws ResourceInitializationException {
    JoshuaConfiguration configuration =
        JoshuaConfiguration.create(
            ImmutableList.of(
                JoshuaTranslationService.SOURCE, SOURCE, JoshuaTranslationService.TARGET, TARGET));

    assertEquals(SOURCE, configuration.getSource());
    assertEquals(TARGET, configuration.getTarget());
    assertEquals(JoshuaTranslationService.DEFAULT_URL, configuration.getUrl());
  }

  @Test
  public void testCanConfigureJoshuaFromStringList() throws ResourceInitializationException {
    JoshuaConfiguration configuration =
        JoshuaConfiguration.create(
            ImmutableList.of(
                JoshuaTranslationService.SOURCE,
                SOURCE,
                JoshuaTranslationService.TARGET,
                TARGET,
                JoshuaTranslationService.URL,
                URL));

    assertEquals(SOURCE, configuration.getSource());
    assertEquals(TARGET, configuration.getTarget());
    assertEquals(URL, configuration.getUrl());
  }
}
