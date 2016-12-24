package uk.gov.dstl.baleen.contentmappers.helpers;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

/**
 * Converts the element into annotations, which should be added to the collector.
 * 
 * The JCas has no text set and likely no annotations - it should only be used to create new
 * annotations.
 * 
 * <pre>
 * collector.add(new Person(jCas));
 * </pre>
 * 
 * Mappers do not need to worry about the begin/end offsets within the text. This is taken care of
 * through the use of the collector.
 *
 */
@FunctionalInterface
public interface ContentMapper {

  void map(JCas jCas, Element element, AnnotationCollector collector);

}
