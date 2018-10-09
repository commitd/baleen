// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import uk.gov.dstl.baleen.translation.TestConfiguredTranslateService;
import uk.gov.dstl.baleen.translation.TestTranslateService;
import uk.gov.dstl.baleen.translation.TranslationException;

public class SharedTranslationResourceTest {

  @Test
  public void canCreateWithService() throws ResourceInitializationException, TranslationException {
    ExternalResourceDescription erd =
        ExternalResourceFactory.createExternalResourceDescription(
            SharedTranslationResource.RESOURCE_KEY,
            SharedTranslationResource.class,
            SharedTranslationResource.PARAM_SERVICE,
            TestTranslateService.class.getSimpleName());

    Resource resource =
        UIMAFramework.produceResource(
            erd.getResourceSpecifier(),
            ImmutableMap.of(Resource.PARAM_UIMA_CONTEXT, UimaContextFactory.createUimaContext()));

    assertTrue(resource instanceof SharedTranslationResource);
    try (SharedTranslationResource translationResource = (SharedTranslationResource) resource) {
      assertEquals(TestTranslateService.DEFAULT_RESPONSE, translationResource.translate(""));
    }
  }

  @Test
  public void canCreateWithConfiguredService()
      throws ResourceInitializationException, TranslationException {

    String config = "PROVIDED CONFIG";

    ExternalResourceDescription erd =
        ExternalResourceFactory.createExternalResourceDescription(
            SharedTranslationResource.RESOURCE_KEY,
            SharedTranslationResource.class,
            SharedTranslationResource.PARAM_SERVICE,
            TestConfiguredTranslateService.class.getSimpleName());

    UimaContext context =
        UimaContextFactory.createUimaContext(TestConfiguredTranslateService.RESPONSE, config);

    Resource resource =
        UIMAFramework.produceResource(
            erd.getResourceSpecifier(), ImmutableMap.of(Resource.PARAM_UIMA_CONTEXT, context));

    assertTrue(resource instanceof SharedTranslationResource);
    try (SharedTranslationResource translationResource = (SharedTranslationResource) resource) {
      assertEquals(config, translationResource.translate(""));
    }
  }
}
