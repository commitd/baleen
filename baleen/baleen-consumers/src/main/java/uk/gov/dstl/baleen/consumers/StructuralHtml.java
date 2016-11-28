// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.consumers;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy;
import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy.Node;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Footnote;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sentence;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableFooter;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;

/**
 * Creates a HTML5 version of the structured annotations of a document.
 *
 * @baleen.javadoc
 */
public class StructuralHtml extends AbstractHtml {

  public void walk(Element parentElement, Node n) {
    final Structure structure = n.getElement();

    if (structure == null || structure.getCoveredText() == null) {
      // Descend into the children directly
      for (final Node child : n.getChildren()) {
        walk(parentElement, child);
      }
    } else {
      final Element e = createTag(structure);
      parentElement.appendChild(e);

      final String text = structure.getCoveredText();
      int offset = 0;
      for (final Node child : n.getChildren()) {
        e.appendText(text.substring(offset, child.getBegin() - n.getBegin()));
        walk(e, child);
        offset = child.getEnd() - n.getBegin();
      }
      e.appendText(text.substring(offset, n.getEnd() - n.getBegin()));


    }

  }

  private Element createElement(String tag) {
    return new Element(Tag.valueOf(tag), "");
  }

  private Element createTag(Structure s) {
    Element e;

    if (s instanceof Anchor || s instanceof Link) {
      e = createElement("a");
    } else if (s instanceof Caption) {
      e = createElement("caption");
    } else if (s instanceof Figure) {
      e = createElement("figure");
    } else if (s instanceof Footer) {
      e = createElement("footer");
    } else if (s instanceof Footnote) {
      e = createElement("aside");
    } else if (s instanceof Header) {
      e = createElement("header");
    } else if (s instanceof Heading) {
      final Heading h = (Heading) s;
      final int level = Math.min(6, Math.max(1, h.getLevel()));
      e = createElement("h" + level);
    } else if (s instanceof Paragraph) {
      e = createElement("p");
    } else if (s instanceof Section) {
      e = createElement("section");
    } else if (s instanceof Sentence) {
      e = createElement("span");
    } else if (s instanceof Style) {
      e = createElement("style");
    } else if (s instanceof Table) {
      e = createElement("table");
    } else if (s instanceof TableBody) {
      e = createElement("tbody");
    } else if (s instanceof TableCell) {
      e = createElement("td");
    } else if (s instanceof TableHeader) {
      e = createElement("thead");
    } else if (s instanceof TableFooter) {
      e = createElement("tfoot");
    } else if (s instanceof TableRow) {
      e = createElement("tr");
    } else {
      e = createElement("div");
    }

    e.attr("class", s.getType().getShortName());

    return e;
  }

  @Override
  protected void writeBody(JCas jCas, Element body) {

    final Node root = StructureHierarchy.build(jCas);

    walk(body, root);
  }

}
