package uk.gov.dstl.baleen.consumers.print;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * Print out all structure.
 */
public class Structures extends BaleenConsumer {

	class Node {

		Structure element;

		public Node(Structure structure) {
			element = structure;
		}

		List<Node> children = new ArrayList<>();

		public void addChlid(Node node) {
			children.add(node);
		}

		public int getBegin() {
			return element == null ? 0 : element.getBegin();
		}

		public int getEnd() {
			return element == null ? Integer.MAX_VALUE : element.getEnd();
		}

		public void print(int level, StringBuilder sb) {
			startTag(level, sb);
			int offset = 0;
			for (Node child : children) {
				addText(level + 1, sb, offset, child.getBegin() - getBegin());
				child.print(level + 1, sb);
				offset = child.getEnd() - getBegin();
			}
			addText(level + 1, sb, offset, getEnd() - getBegin());
			endTag(level, sb);
		}

		private void addText(int level, StringBuilder sb, int start, int end) {

			if (element != null && start < end) {
				String coveredText = element.getCoveredText();
				if (start > end || end > coveredText.length()) {
					sb.append("ERROR IN DOCUMENT TREE");
				}
				indent(level, sb);
				sb.append(coveredText.substring(start, end));
			}
		}

		private void indent(int size, StringBuilder sb) {
			sb.append("\n");
			for (int i = 0; i < size - 1; i++) {
				sb.append("\t");
			}
		}

		private void endTag(int level, final StringBuilder sb) {
			if (element != null) {
				indent(level, sb);
				sb.append("</");
				sb.append(element.getClass().getSimpleName());
				sb.append(">");
			}
		}

		private void startTag(int level, final StringBuilder sb) {
			if (element != null) {
				indent(level, sb);
				sb.append("<");
				sb.append(element.getClass().getSimpleName());
				sb.append(">");
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			print(0, sb);
			return sb.toString();
		}

	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		List<Structure> select = new ArrayList<>(JCasUtil.select(jCas, Structure.class));
		Collections.sort(select, new Comparator<Structure>() {

			@Override
			public int compare(Structure s1, Structure s2) {
				int compare = Integer.compare(s1.getBegin(), s2.getBegin());
				if (compare == 0) {
					compare = Integer.compare(s2.getEnd(), s1.getEnd());
				}
				return compare;
			}
		});

		Node parent = new Node(null);
		Stack<Node> stack = new Stack<>();
		stack.push(parent);

		select.forEach(s -> build(stack, s));

		String result = print(parent);

		getMonitor().info("{}:\n{}", Structure.class.getName(), result);
	}

	private String print(Node parent) {
		StringBuilder sb = new StringBuilder();
		parent.print(0, sb);
		return sb.toString();
	}

	private void build(Stack<Node> stack, Structure s) {
		Node parent = getParent(stack, s);
		Node node = new Node(s);
		parent.addChlid(node);
		stack.push(node);
	}

	private Node getParent(Stack<Node> stack, Structure s) {
		while (stack.peek().getEnd() <= s.getBegin()) {
			stack.pop();
		}
		return stack.peek();
	}

}
