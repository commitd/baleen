// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.resources.TranslatingJCas;

/**
 * Extracts the content assuming it is plain text then translates it
 *
 * @baleen.javadoc
 */
public class TranslatingPlainTextContentExtractor extends PlainTextContentExtractor {

  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  protected SharedTranslationResource translationService;

  /** Default constructor for UIMA */
  public TranslatingPlainTextContentExtractor() {
    // DO NOTHING
  }

  /** Constructor for testing */
  protected TranslatingPlainTextContentExtractor(SharedTranslationResource translationService) {
    this.translationService = translationService;
  }

  @Override
  public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {
    JCas tempJcas = new TranslatingJCas(jCas, translationService);
    super.doProcessStream(stream, source, tempJcas);
  }
}
