package uk.gov.dstl.baleen.consumers.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.types.structure.Structure;

/**
 * Print out all structure.
 */
public class StructureHierarchy {

  public static final class Node {

    private final Structure element;

    private final List<Node> children = new ArrayList<>();

    public Node(Structure structure) {
      element = structure;
    }

    public void addChild(Node node) {
      children.add(node);
    }

    public int getBegin() {
      return element == null ? 0 : element.getBegin();
    }

    public List<Node> getChildren() {
      return children;
    }

    public Structure getElement() {
      return element;
    }

    public int getEnd() {
      return element == null ? Integer.MAX_VALUE : element.getEnd();
    }

  }

  public static Node build(JCas jCas) {
    final List<Structure> select = new ArrayList<>(JCasUtil.select(jCas, Structure.class));
    Collections.sort(select, (s1, s2) -> {
      int compare = Integer.compare(s1.getBegin(), s2.getBegin());
      if (compare == 0) {
        compare = Integer.compare(s2.getEnd(), s1.getEnd());
      }
      if (compare == 0) {
        compare = Integer.compare(s1.getDepth(), s2.getDepth());
      }
      return compare;
    });

    final Node parent = new Node(null);
    final Deque<Node> deque = new ArrayDeque<>();
    deque.push(parent);

    select.forEach(s -> build(deque, s));

    return parent;

  }

  private static void build(Deque<Node> deque, Structure s) {
    final Node parent = getParent(deque, s);
    final Node node = new Node(s);
    parent.addChild(node);
    deque.push(node);
  }

  private static Node getParent(Deque<Node> deque, Structure s) {
    while (deque.peek().getEnd() <= s.getBegin()) {
      deque.pop();
    }
    return deque.peek();
  }


  private StructureHierarchy() {
    // Create through build
  }

}
