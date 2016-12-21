package uk.gov.dstl.baleen.contentmanipulators;

import org.jsoup.nodes.Document;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;

public class UntagEmptyText implements ContentManipulator {

  @Override
  public void manipulate(final Document document) {
    document.select(":empty").remove();
  }

}
