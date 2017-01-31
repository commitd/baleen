package uk.gov.dstl.baleen.contentmappers;

import static org.junit.Assert.assertEquals;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.types.metadata.Metadata;

public class MetaTagTest {


  @Test
  public void testNameContent() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final MetaTags mt = new MetaTags();

    final Element element = new Element(Tag.valueOf("meta"), "");
    element.attr("name", "key");
    element.attr("content", "value");

    final AnnotationCollector collector = new AnnotationCollector();
    mt.map(jCas, element, collector);
    final Metadata annotation = (Metadata) collector.getAnnotations().get(0);
    assertEquals("key", annotation.getKey());
    assertEquals("value", annotation.getValue());

  }

  @Test
  public void testCharset() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final MetaTags mt = new MetaTags();

    final Element element = new Element(Tag.valueOf("meta"), "");
    element.attr("charset", "UTF");

    final AnnotationCollector collector = new AnnotationCollector();
    mt.map(jCas, element, collector);
    final Metadata annotation = (Metadata) collector.getAnnotations().get(0);
    assertEquals("UTF", annotation.getValue());

  }
}
