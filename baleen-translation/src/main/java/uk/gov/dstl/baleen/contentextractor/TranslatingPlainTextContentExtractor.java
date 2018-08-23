package uk.gov.dstl.baleen.contentextractor;

import java.io.IOException;
import java.io.InputStream;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.contentextractors.PlainTextContentExtractor;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;

public class TranslatingPlainTextContentExtractor extends PlainTextContentExtractor {


  /**
   * Translation service
   *
   * @baleen.resource uk.gov.dstl.baleen.resources.SharedTranslationResource
   */
  public static final String RESOURCE_KEY = SharedTranslationResource.RESOURCE_KEY;

  @ExternalResource(key = RESOURCE_KEY)
  protected SharedTranslationResource translationService;


  /**
   * Default constructor for UIMA
   */
  public TranslatingPlainTextContentExtractor() {
    // DO NOTHING
  }

  /**
   * Default constructor for UIMA
   */
  protected TranslatingPlainTextContentExtractor(SharedTranslationResource translationService) {
    this.translationService = translationService;
  }

  @Override
  public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {
    JCas tempJcas = createTempJCas();
    super.doProcessStream(stream, source, tempJcas);

    String translation;
    try {
      translation = translationService.translate(tempJcas.getDocumentText());
    } catch (TranslationException e) {
      getMonitor().warn("Unable to translate {}", source);
      translation = tempJcas.getDocumentText();
    }

    jCas.setDocumentText(translation);

  }


  private JCas createTempJCas() throws IOException {
    try {
      return JCasFactory.createJCas();
    } catch (UIMAException e) {
      throw new IOException(e);
    }
  }
}
