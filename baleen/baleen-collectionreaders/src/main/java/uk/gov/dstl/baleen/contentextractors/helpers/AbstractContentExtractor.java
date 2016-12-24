// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.contentextractors.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.BaleenContentExtractor;

/**
 * Provides a basis for content extractors, implementing common functionality.
 * 
 * Sets the source and timestamp of the document, and the extraction class as metadata.
 * 
 */
public abstract class AbstractContentExtractor extends BaleenContentExtractor {
  private static final String METADATA_KEY_CONTENT_EXTRACTOR = "baleen:content-extractor";

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.uima.BaleenContentExtractor#doProcessStream(java.io.InputStream,
   * java.lang.String, org.apache.uima.jcas.JCas)
   */
  @Override
  public void doProcessStream(final InputStream stream, final String source, final JCas jCas)
      throws IOException {
    final DocumentAnnotation doc = getSupport().getDocumentAnnotation(jCas);
    doc.setSourceUri(source);
    doc.setTimestamp(System.currentTimeMillis());

    // Add metadata item to capture which content extractor was used
    addMetadata(jCas, METADATA_KEY_CONTENT_EXTRACTOR, this.getClass().getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.uima.BaleenContentExtractor#doInitialize(org.apache.uima.UimaContext,
   * java.util.Map)
   */
  @Override
  public void doInitialize(final UimaContext context, final Map<String, Object> params)
      throws ResourceInitializationException {
    ConfigurationParameterInitializer.initialize(this, params);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.gov.dstl.baleen.uima.BaleenContentExtractor#doDestroy()
   */
  @Override
  public void doDestroy() {
    // Do nothing
  }
}
