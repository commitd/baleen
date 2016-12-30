package uk.gov.dstl.baleen.uima.data;

import java.util.Collection;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import uk.gov.dstl.baleen.types.language.Text;

public class TextBlock {
  private final JCas jCas;

  private final Text text;

  private final int blockBegin;

  private final int blockEnd;

  public TextBlock(final JCas jCas, final Text text) {
    this.jCas = jCas;
    this.text = text;
    this.blockBegin = text.getBegin();
    this.blockEnd = text.getEnd();
  }

  public TextBlock(final JCas jCas) {
    this.jCas = jCas;
    this.blockBegin = 0;
    this.blockEnd = jCas.getDocumentText().length();
    this.text = null;

  }

  public boolean isWholeDocument() {
    return text != null;
  }

  public Text getText() {
    return text;
  }

  public JCas getJCas() {
    return jCas;
  }

  public int getBegin() {
    return blockBegin;
  }

  public int getEnd() {
    return blockEnd;
  }

  public String getCoveredText() {
    if (isWholeDocument()) {
      return jCas.getDocumentText();
    } else {
      return text.getCoveredText();
    }
  }

  public String getDocumentText() {
    return jCas.getDocumentText();
  }

  // JCasUtil helpers

  public <T extends Annotation> Collection<T> select(final Class<T> type) {
    if (isWholeDocument()) {
      return JCasUtil.select(jCas, type);
    } else {
      return JCasUtil.selectCovered(jCas, type, getBegin(), getEnd());

    }
  }

  // Creating annotation helpers

  public <T extends Annotation> T newAnnotation(final Class<T> type, final int begin,
      final int end) {

    try {
      return type.getConstructor(JCas.class, int.class, int.class)
          .newInstance(jCas, toDocumentOffset(begin), toDocumentOffset(end));
    } catch (final Exception e) {
      throw new RuntimeException("Required type not found", e);
    }
  }

  public <T extends Annotation> T setBeginAndEnd(final T annotation, final int begin,
      final int end) {
    annotation.setBegin(toDocumentOffset(begin));
    annotation.setEnd(toDocumentOffset(end));
    return annotation;
  }

  public int toDocumentOffset(final int blockOffset) {
    return blockOffset + getBegin();
  }
}
