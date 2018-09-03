// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.resources.TranslatingJCas;

/**
 * Extracts metadata and text content from the supplied input, using Apache Tika then translates the
 * text using the {@link SharedTranslationResource}.
 *
 * @baleen.javadoc
 */
public class TranslatingTikaContentExtractor extends TikaContentExtractor {

  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  protected SharedTranslationResource translationService;

  /** Default constructor for UIMA */
  public TranslatingTikaContentExtractor() {
    // DO NOTHING
  }

  /** Constructor for testing */
  protected TranslatingTikaContentExtractor(SharedTranslationResource translationService) {
    this.translationService = translationService;
  }

  @Override
  public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {
    JCas tempJcas = new TranslatingJCas(jCas, translationService);
    super.doProcessStream(stream, source, tempJcas);
  }
}
