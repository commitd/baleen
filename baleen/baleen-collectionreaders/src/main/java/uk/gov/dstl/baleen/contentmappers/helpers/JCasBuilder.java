package uk.gov.dstl.baleen.contentmappers.helpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import uk.gov.dstl.baleen.types.structure.Structure;

/**
 * Holds and constructs text and annotations whilst the HTML is being converted to JCas.
 * 
 * Note that build should only be called once (as it sets the text of the jCas).
 *
 */
public class JCasBuilder {
  private final List<Annotation> annotations = new LinkedList<>();
  private final StringBuilder documentText = new StringBuilder();
  private final JCas jCas;

  /**
   * Instantiates a new builder.
   *
   * @param jCas the jCas
   */
  public JCasBuilder(final JCas jCas) {
    this.jCas = jCas;

  }

  /**
   * Gets the jcas.
   *
   * @return the jcas
   */
  public JCas getJCas() {
    return jCas;
  }

  /**
   * Gets the current offset within the underconstruction text buffer.
   *
   * @return the current offset
   */
  public int getCurrentOffset() {
    return documentText.length();
  }

  /**
   * Adds text.
   *
   * @param text the text
   */
  public void addText(final String text) {
    documentText.append(text);
  }

  /**
   * Adds annotations.
   *
   * @param collection the collection
   * @param begin the begin
   * @param end the end
   * @param depth the depth (within the tags)
   */
  public void addAnnotations(final Collection<Annotation> collection, final int begin,
      final int end, final int depth) {
    collection.forEach(a -> {
      a.setBegin(begin);
      a.setEnd(end);
      if (a instanceof Structure) {
        ((Structure) a).setDepth(depth);
      }
      annotations.add(a);
    });
  }

  /**
   * Apply the text and annotations to the jCas.
   * 
   * Once call once.
   */
  public void build() {
    jCas.setDocumentText(documentText.toString());
    for (final Annotation a : annotations) {
      a.addToIndexes(jCas);
    }
  }
}
