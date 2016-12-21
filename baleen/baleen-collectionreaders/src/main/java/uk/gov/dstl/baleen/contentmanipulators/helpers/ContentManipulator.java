package uk.gov.dstl.baleen.contentmanipulators.helpers;

import org.jsoup.nodes.Document;

@FunctionalInterface
public interface ContentManipulator {

  void manipulate(Document document);

}
