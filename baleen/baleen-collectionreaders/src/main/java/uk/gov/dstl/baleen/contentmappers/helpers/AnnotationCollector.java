package uk.gov.dstl.baleen.contentmappers.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;

public class AnnotationCollector {
  private List<Annotation> annotations;

  public void add(final Annotation... a) {
    add(Arrays.asList(a));
  }

  private void add(final Collection<Annotation> collection) {
    if (annotations == null) {
      annotations = new LinkedList<>();
    }
    annotations.addAll(collection);
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }
}
