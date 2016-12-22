package uk.gov.dstl.baleen.contentmanipulators;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;

public class RemoveEmptyText implements ContentManipulator {

  @Override
  public void manipulate(final Document document) {
    while (!removeEmpty(document)) {
      // Repeat as needed.... work done in the while
    }
  }

  private boolean removeEmpty(final Document document) {
    final Elements emptyNodes = document.select(":empty");
    if (emptyNodes.isEmpty()) {
      return true;
    }
    emptyNodes.remove();
    return false;
  }

}
