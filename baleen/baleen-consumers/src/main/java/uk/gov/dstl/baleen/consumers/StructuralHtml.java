// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.consumers;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.google.common.base.Strings;

import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy;
import uk.gov.dstl.baleen.consumers.utils.StructureHierarchy.Node;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Footnote;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Page;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sentence;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableFooter;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.TextDocument;
import uk.gov.dstl.baleen.types.structure.Unordered;

/**
 * Creates a HTML5 version of the structured annotations of a document.
 *
 * This will NOT output entity, relation or other semantic annotations.
 *
 * @baleen.javadoc
 */
public class StructuralHtml extends AbstractHtml {

  /**
   * Apply styling information in the original document to the output.
   *
   * Examples include colour, underline, etc.
   *
   * Unless your documents encode important information through styles, you should use a CSS style
   * sheet and leave this off.
   *
   *
   * @baleen.config true
   */

  public static final String PARAM_APPLY_STYLES = "applyStyles";
  @ConfigurationParameter(name = PARAM_APPLY_STYLES, defaultValue = "true")
  private Boolean applyStyles;

  /**
   * Outputs data-* attributes on the tags using Baleen information (begin, end, id, etc).
   *
   * This increases the overall size of the HTML, but if very useful for onward machine processing.
   *
   * @baleen.config false
   */

  public static final String PARAM_OUTPUT_DATA = "outputData";
  @ConfigurationParameter(name = PARAM_OUTPUT_DATA, defaultValue = "false")
  private Boolean outputData;

  /**
   * Output empty tags.
   *
   * Should tags which have no text and no content be output to the HTML.
   *
   * There is little reason to do this unless debugging the structural processing of Baleen, as it
   * unnecessarily complicates the documents.
   *
   * @baleen.config false
   */
  public static final String PARAM_OUTPUT_EMPTY_TAGS = "outputEmptyTags";
  @ConfigurationParameter(name = PARAM_OUTPUT_EMPTY_TAGS, defaultValue = "false")
  private Boolean outputEmptyTags;

  public boolean walk(Element parentElement, Node n) {
    final Structure structure = n.getElement();

    // TODO: Here we always create a new element, but in reality we could use parentElement if the
    // element is just a div (that is structure == null etc)
    // That might clean up the HTML
    final Element e = createTag(structure);

    boolean added = false;

    if (structure == null || structure.getCoveredText() == null) {
      // Descend into the children directly
      for (final Node child : n.getChildren()) {
        final boolean addedChild = walk(e, child);
        if (addedChild) {
          added = true;
        }
      }
    } else {

      final String text = structure.getCoveredText();
      int offset = 0;
      for (final Node child : n.getChildren()) {
        final boolean addedText = appendText(e, text, offset, child.getBegin() - n.getBegin());
        final boolean addedChild = walk(e, child);
        offset = child.getEnd() - n.getBegin();

        if (addedChild || addedText) {
          added = true;
        }
      }
      final boolean addedText = appendText(e, text, offset, n.getEnd() - n.getBegin());
      if (addedText) {
        added = true;
      }
    }

    if (added || outputEmptyTags) {
      parentElement.appendChild(e);
    }

    return added;
  }

  private boolean appendText(Element e, String text, int start, int end) {
    if (start < end && end <= text.length()) {
      e.appendText(text.substring(start, end));
      return true;
    } else {
      return false;
    }
  }

  private String buildCssStyle(Style s) {
    final String color = s.getColor();
    final StringArray decorations = s.getDecoration();
    final String font = s.getFont();

    // If no style info stop
    if (Strings.isNullOrEmpty(color) && Strings.isNullOrEmpty(font)
        && (decorations == null || decorations.size() == 0)) {
      return null;
    }

    final StringBuilder sb = new StringBuilder();

    // This is very naive, it a passthrough of the original formats values
    // Effectively we are just hoping the browser knows what to do.

    if (!Strings.isNullOrEmpty(color)) {
      sb.append(String.format("color:%s; ", color));
    }

    if (!Strings.isNullOrEmpty(font)) {
      sb.append(String.format("font-family:\"%s\"; ", color));
    }

    if (decorations != null && decorations.size() > 0) {
      final String[] array = decorations.toArray();
      for (final String a : array) {
        switch (a.toUpperCase()) {
          case "UNDERLINE":
            sb.append("text-decoration:underline; ");
            break;
          case "BOLD":
            sb.append("font-weight:bold; ");
            break;
          case "ITALICS":
            sb.append("font-style:italic; ");
            break;
          default:
            // No nothing - we don't know what it means
            break;
        }
      }
    }

    return sb.toString();
  }

  private Element createElement(String tag) {
    return new Element(Tag.valueOf(tag), "");
  }

  private Element createTag(Structure s) {
    Element e;

    if (s == null) {
      e = createElement("div");
    } else if (s instanceof Anchor) {
      e = createElement("a");
      e.attr("id", s.getExternalId());
    } else if (s instanceof Caption) {
      e = createElement("caption");
    } else if (s instanceof Document || s instanceof SpreadSheet || s instanceof SlideShow
        || s instanceof TextDocument) {
      e = createElement("main");
    } else if (s instanceof Figure) {
      // TODO This is more complex I guess if we really wanted to put in a img / object tag.
      // but we don't have that info.
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
    } else if (s instanceof Link) {
      e = createElement("a");
      e.attr("href", ((Link) s).getTarget());
    } else if (s instanceof ListItem) {
      e = createElement("li");
    } else if (s instanceof Ordered) {
      e = createElement("ol");
    } else if (s instanceof Unordered) {
      e = createElement("ul");
    } else if (s instanceof Page || s instanceof Slide || s instanceof Sheet) {
      e = createElement("article");
    } else if (s instanceof Paragraph) {
      e = createElement("p");
    } else if (s instanceof Section) {
      e = createElement("section");
    } else if (s instanceof Sentence) {
      e = createElement("span");
    } else if (s instanceof Style) {
      e = createElement("span");
      if (applyStyles) {
        final String cssStyle = buildCssStyle((Style) s);
        if (!Strings.isNullOrEmpty(cssStyle)) {
          e.attr("style", cssStyle);
        }
      }
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

    if (s != null) {
      e.attr("class",
          String.format("baleen-structure-%s", s.getType().getShortName().toLowerCase()));

      // Add generic data attributes
      if (outputData) {
        e.attr("data-baleen-structure-depth", Integer.toString(s.getDepth()));
        e.attr("data-baleen-id", s.getExternalId());
        e.attr("data-baleen-begin", Integer.toString(s.getBegin()));
        e.attr("data-baleen-end", Integer.toString(s.getEnd()));
      }
    }

    return e;
  }

  @Override
  protected void writeBody(JCas jCas, Element body) {

    final Node root = StructureHierarchy.build(jCas);

    walk(body, root);

    // We need to create the proper li tags under ol and ul

    body.select("ul > p").wrap("<li></li>");
    body.select("ol > p").wrap("<li></li>");


    // TODO: In accordance with HTML spec
    // - Captions for Table should be moved inside the table
    // - Captions for Figure should be moved inside the figure
    // Best way to achieve some to fthese would be to walk the root node before creating HTML?

  }

}
