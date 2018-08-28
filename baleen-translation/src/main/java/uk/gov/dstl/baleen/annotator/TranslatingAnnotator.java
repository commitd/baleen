// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.annotator;

import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.tcas.Annotation;

import com.google.common.collect.ImmutableSet;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.types.language.Translation;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;

/**
 * This {@link BaleenTextAwareAnnotator} adds translation annotations to each {@link TextBlock}
 * using the {@link SharedTranslationResource}.
 */
public class TranslatingAnnotator extends BaleenTextAwareAnnotator {

  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  private SharedTranslationResource translationService;

  /**
   * Optionally check that the source language matches the expected source of the language service.
   */
  public static final String PARAM_CHECK_SOURCE_LANGUAGE = "checkSourceLanguage";

  @ConfigurationParameter(name = PARAM_CHECK_SOURCE_LANGUAGE, defaultValue = "false")
  private boolean checkSourceLanguage;

  @Override
  protected void doProcessTextBlock(TextBlock block) throws AnalysisEngineProcessException {

    if (checkSourceLanguage) {
      String documentLanguage = block.getJCas().getDocumentLanguage();
      String language = translationService.getSourceLanguage();
      if (!("x-unspecified".equals(documentLanguage) || documentLanguage.startsWith(language))) {
        return;
      }
    }

    String coveredText = block.getCoveredText();
    try {
      String translation = translationService.translate(coveredText);
      Translation annotation = block.newAnnotation(Translation.class, 0, coveredText.length());
      annotation.setLanguage(translationService.getTargetLanguage());
      annotation.setTranslation(translation);
      addToJCasIndex(annotation);
    } catch (TranslationException e) {
      getMonitor().debug("Unable to translate {}", coveredText);
    }
  }

  @Override
  protected Set<Class<? extends Annotation>> getOutputTypes() {
    return ImmutableSet.of(Translation.class);
  }
}
