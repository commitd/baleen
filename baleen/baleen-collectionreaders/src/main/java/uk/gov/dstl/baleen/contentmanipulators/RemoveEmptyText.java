package uk.gov.dstl.baleen.contentmanipulators;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;

/**
 * Recursives remove empty HTML tags to clean the document.
 * 
 * This will not remove the body tag, but everything either will be remove it is empty (or only
 * holds empty elements).
 *
 */
public class RemoveEmptyText implements ContentManipulator {

  @Override
  public void manipulate(final Document document) {
    final Element body = document.body();

    while (!removeEmpty(body)) {
      // Repeat as needed.... work done in the while
    }
  }

  private boolean removeEmpty(final Element document) {
    final Elements emptyNodes = document.select(":empty").not("body");
    if (emptyNodes.isEmpty()) {
      return true;
    }
    emptyNodes.remove();
    return false;
  }

}
