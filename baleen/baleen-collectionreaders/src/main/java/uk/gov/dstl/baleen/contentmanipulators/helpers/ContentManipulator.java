package uk.gov.dstl.baleen.contentmanipulators.helpers;

import org.jsoup.nodes.Document;

/**
 * Manipulate the HTML content in arbitrary manner.
 *
 */
@FunctionalInterface
public interface ContentManipulator {

  void manipulate(Document document);

}
