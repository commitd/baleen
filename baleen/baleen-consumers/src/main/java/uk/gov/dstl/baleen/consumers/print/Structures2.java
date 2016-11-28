package uk.gov.dstl.baleen.consumers.print;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy;
import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy.Node;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * Print out all structure.
 */
public class Structures2 extends BaleenConsumer {

  public void print(int level, StringBuilder sb, Node n) {
    startTag(level, sb, n.getElement());
    int offset = 0;
    for (final Node child : n.getChildren()) {
      addText(level + 1, sb, n.getElement(), offset, child.getBegin() - n.getBegin());
      print(level + 1, sb, child);
      offset = child.getEnd() - n.getBegin();
    }
    addText(level + 1, sb, n.getElement(), offset, n.getEnd() - n.getBegin());
    endTag(level, sb, n.getElement());
  }

  private void addText(int level, StringBuilder sb, Structure element, int start, int end) {

    if (element != null && start < end) {
      final String coveredText = element.getCoveredText();
      if (start > end || end > coveredText.length()) {
        sb.append("ERROR IN DOCUMENT TREE");
      }
      indent(level, sb);
      sb.append(coveredText.substring(start, end));
    }
  }

  private void endTag(int level, final StringBuilder sb, Structure element) {
    if (element != null) {
      indent(level, sb);
      sb.append("</");
      sb.append(element.getClass().getSimpleName());
      sb.append(">");
    }
  }

  private void indent(int size, StringBuilder sb) {
    sb.append("\n");
    for (int i = 0; i < size - 1; i++) {
      sb.append("\t");
    }
  }

  private String print(Node parent) {
    final StringBuilder sb = new StringBuilder();
    print(0, sb, parent);
    return sb.toString();
  }

  private void startTag(int level, final StringBuilder sb, Structure element) {
    if (element != null) {
      indent(level, sb);
      sb.append("<");
      sb.append(element.getClass().getSimpleName());
      sb.append(">");
    }
  }

  @Override
  protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
    final Node parent = StructureHierarchy.build(jCas);

    final String result = print(parent);

    getMonitor().info("{}:\n{}", Structure.class.getName(), result);
  }

}
