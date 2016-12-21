package uk.gov.dstl.baleen.contentmappers.helpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

public class JCasBuilder {
  private final List<Annotation> annotations = new LinkedList<>();
  private final StringBuilder documentText = new StringBuilder();
  private final JCas jCas;

  public JCasBuilder(final JCas jCas) {
    this.jCas = jCas;

  }

  public JCas getJCas() {
    return jCas;
  }

  public int getCurrentOffset() {
    return documentText.length();
  }

  public void addText(final String text) {
    documentText.append(text);
  }

  public void addAnnotations(final Collection<Annotation> collection, final int begin,
      final int end, final int depth) {
    collection.forEach(a -> {
      a.setBegin(begin);
      a.setEnd(end);
      annotations.add(a);
    });
  }

  public void build() {
    jCas.setDocumentText(documentText.toString());
    for (final Annotation a : annotations) {
      a.addToIndexes(jCas);
    }
  }
}
