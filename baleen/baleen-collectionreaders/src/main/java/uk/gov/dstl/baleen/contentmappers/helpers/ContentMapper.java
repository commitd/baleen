package uk.gov.dstl.baleen.contentmappers.helpers;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

@FunctionalInterface
public interface ContentMapper {

  void map(JCas jCas, Element element, AnnotationCollector collector);

}
