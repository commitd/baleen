package uk.gov.dstl.baleen.contentmanipulators;

import org.jsoup.nodes.Document;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;

/**
 * Removes the header and footer from document.
 */
public class HeaderAndFooterRemover implements ContentManipulator {

  @Override
  public void manipulate(final Document document) {
    document.select("header").remove();
    document.select("footer").remove();
  }

}