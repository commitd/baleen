package uk.gov.dstl.baleen.contentmanipulators.helpers;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.jsoup.nodes.Document;

/**
 * Manipulate the HTML content in arbitrary manner.
 *
 */
@FunctionalInterface
public interface ContentManipulator {

  void manipulate(Document document);

  /**
   * Provided UIMA context for initialisation as per Uima initialise.
   * 
   * Largely not required by implementation.
   * 
   * @param context
   * @throws ResourceInitializationException
   */
  default void initialize(final UimaContext context) throws ResourceInitializationException {
    // Do nothing
  }

  /**
   * Called when the pipeline is destroyed
   * 
   */
  default void destroy() {
    // Do nothing
  }


}
