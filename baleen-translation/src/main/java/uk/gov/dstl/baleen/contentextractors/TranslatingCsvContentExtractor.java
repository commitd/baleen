// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.resources.TranslatingJCas;

/**
 * Takes a single line of CSV data, and splits it into 'columns' based on the specified separator
 * character. The column designated as the main content is first translated then set as the JCas
 * body, and other columns are added as Metadata annotations.
 *
 * @baleen.javadoc
 */
public class TranslatingCsvContentExtractor extends CsvContentExtractor {

  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  protected SharedTranslationResource translationService;

  /** Default constructor for UIMA */
  public TranslatingCsvContentExtractor() {
    // DO NOTHING
  }

  /** Constructor for testing */
  protected TranslatingCsvContentExtractor(SharedTranslationResource translationService) {
    this.translationService = translationService;
  }

  @Override
  public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {
    JCas tempJcas = new TranslatingJCas(jCas, translationService);
    super.doProcessStream(stream, source, tempJcas);
  }
}
