package uk.gov.dstl.baleen.contentextractors.helpers;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.google.common.base.Strings;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.contentmappers.helpers.JCasBuilder;

public class DocumentToJCasConverter {

  private final List<ContentMapper> mappers;

  public DocumentToJCasConverter(final List<ContentMapper> mappers) {
    this.mappers = mappers;
  }

  public void apply(final Document document, final JCas jCas) {

    final JCasBuilder builder = new JCasBuilder(jCas);

    walk(builder, document.body(), 1);

    builder.build();
  }

  private void walk(final JCasBuilder builder, final Node root, final int depth) {
    if (root == null) {
      return;
    }

    final int begin = builder.getCurrentOffset();

    // Generate the text and the annotations
    final String text = mapToText(root);
    if (!Strings.isNullOrEmpty(text)) {
      builder.addText(text);
    }

    List<Annotation> annotations = null;
    if (root instanceof Element) {
      annotations = mapElementToAnnotations(builder.getJCas(), (Element) root);
    }

    // BUG: With multiple mappers depth here is wrong! It puts all mappers at the same depth...
    // (though in fairness they are all the same begin-end and same element too)

    // Walk the children
    for (final Node node : root.childNodes()) {
      walk(builder, node, depth + 1);
    }

    // Add annotations to the JCas
    final int end = builder.getCurrentOffset();
    if (annotations != null && !annotations.isEmpty()) {
      builder.addAnnotations(annotations, begin, end, depth);
    }
  }

  private String mapToText(final Node node) {
    if (node instanceof TextNode) {
      final TextNode t = (TextNode) node;
      return t.getWholeText();
    } else {
      return null;
    }
  }

  private List<Annotation> mapElementToAnnotations(final JCas jCas,
      final Element element) {
    final AnnotationCollector collector = new AnnotationCollector();
    for (final ContentMapper mapper : mappers) {
      mapper.map(jCas, element, collector);
    }
    return collector.getAnnotations();
  }

}
