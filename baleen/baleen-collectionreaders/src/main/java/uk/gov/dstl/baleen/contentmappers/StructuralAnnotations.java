package uk.gov.dstl.baleen.contentmappers;

import java.util.Collections;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Aside;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.DefinitionDescription;
import uk.gov.dstl.baleen.types.structure.DefinitionItem;
import uk.gov.dstl.baleen.types.structure.DefinitionList;
import uk.gov.dstl.baleen.types.structure.Details;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Preformatted;
import uk.gov.dstl.baleen.types.structure.Quotation;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Summary;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableFooter;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.Unordered;
import uk.gov.dstl.baleen.uima.utils.UimaTypesUtils;

public class StructuralAnnotations implements ContentMapper {

  @Override
  public void map(final JCas jCas, final Element element, final AnnotationCollector collector) {
    switch (element.tagName().toLowerCase()) {

      case "p":
        collector.add(new Paragraph(jCas));
        break;

      // Headings

      case "h1":
        createHeading(jCas, collector, 1);
        break;
      case "h2":
        createHeading(jCas, collector, 2);
        break;
      case "h3":
        createHeading(jCas, collector, 3);
        break;
      case "h4":
        createHeading(jCas, collector, 4);
        break;
      case "h5":
        createHeading(jCas, collector, 5);
        break;
      case "h6":
        createHeading(jCas, collector, 6);
        return;

      // Lists

      case "ul":
        collector.add(new Unordered(jCas));
        break;

      case "ol":
        collector.add(new Ordered(jCas));
        break;

      case "li":
        collector.add(new ListItem(jCas));
        break;

      case "dl":
        collector.add(new DefinitionList(jCas));
        break;
      case "dt":
        collector.add(new DefinitionItem(jCas));
        break;
      case "dd":
        // TODO: It might make sense to refer the dt wihtin the type system (setTerm())
        collector.add(new DefinitionDescription(jCas));
        break;

      // Table

      case "table":
        collector.add(new Table(jCas));
        break;

      case "thead":
        collector.add(new TableHeader(jCas));
        break;

      case "tfoot":
        collector.add(new TableFooter(jCas));
        break;

      case "tbody":
        collector.add(new TableBody(jCas));
        break;

      case "tr":
        final TableRow tr = new TableRow(jCas);
        tr.setRow(findRowIndexOfRow(element));
        collector.add(tr);
        break;

      case "th":
        // fall through
      case "td":
        final TableCell td = new TableCell(jCas);
        td.setColumn(findColIndexOfCell(element));
        td.setRow(findRowIndexOfCell(element));
        td.setRowSpan(getIntegerAttribute(element, "rowspan", 1));
        td.setColumnSpan(getIntegerAttribute(element, "colspan", 1));
        collector.add(td);
        break;

      // Links and anchors

      case "a":
        createAnchor(jCas, collector, element);
        break;

      // Images

      case "audio":
      case "video":
      case "embed":
      case "object":
      case "img":
      case "map":
      case "area":
      case "canvas":
      case "figure":
        collector.add(new Figure(jCas));
        break;

      case "caption":
      case "figcaption":
        collector.add(new Caption(jCas));
        break;

      // Styling

      case "ins":
        // fall through - HTML W3 http://www.w3schools.com/tags/tag_ins.asp says that ins would
        // normally be underlined
      case "u":
        createStyle(jCas, collector, "underline");
        break;

      case "i":
      case "em":
        createStyle(jCas, collector, "italics");
        break;

      case "b":
      case "strong":
        createStyle(jCas, collector, "bold");
        break;

      case "strike":
      case "s":
      case "del":
        createStyle(jCas, collector, "strike");
        break;

      case "sup":
        createStyle(jCas, collector, "superscript");
        break;

      case "sub":
        createStyle(jCas, collector, "subscript");
        break;

      case "small":
        createStyle(jCas, collector, "small");
        break;

      case "big":
        // Not HTML5 so not likely to be seen
        createStyle(jCas, collector, "big");
        break;

      case "mark":
        createStyle(jCas, collector, "highlighted");
        break;

      // Purely structural

      case "aside":
        collector.add(new Aside(jCas));
        break;
      case "details":
        collector.add(new Details(jCas));
        break;
      case "summary":
        collector.add(new Summary(jCas));
        break;
      case "section":
      case "div":
        // Div means very little nothing... but we wrap it in a section
        collector.add(new Section(jCas));
        break;

      case "span":
        // Do nothing
        break;

      case "main":
        createFromMain(jCas, collector, element);
        break;
      case "article":
        createFromArticle(jCas, collector, element);
        break;
      case "header":
        collector.add(new Header(jCas));
        break;
      case "footer":
        collector.add(new Footer(jCas));
        break;

      case "kbd":
      case "samp":
      case "code":
      case "pre":
        collector.add(new Preformatted(jCas));
        break;

      case "blockquote":
        collector.add(new Section(jCas));
        // Fall through
      case "q":
        collector.add(new Quotation(jCas));
        break;

      // Potential semantic types, but left to other mappers to actually annotate
      case "time":
      case "meter":
      case "dfn":
      case "address":
      case "abbr":
      case "cite":
        return;

      // Misc ignored - head, details of embedded, ui specific, forms
      case "html":
      case "head":
      case "title":
      case "meta":
      case "base":
      case "style":
      case "script":
      case "noscript":
      case "link":
      case "hr":
      case "dialog":
      case "nav":
      case "menu":
      case "menuitem":
      case "param":
      case "track":
      case "source":
      case "iframe":
      case "form":
      case "input":
      case "textarea":
      case "button":
      case "select":
      case "optgroup":
      case "option":
      case "label":
      case "fieldset":
      case "legend":
      case "datalist":
      case "keygen":
      case "output":
      case "ruby":
      case "rt":
      case "rp":
      case "progress":
      case "bdo":
      case "bdi":
      default:
        return;
    }
  }

  private int getIntegerAttribute(final Element element, final String key, final int defaultValue) {
    final String value = element.attr(key);
    if (Strings.isNullOrEmpty(value)) {
      return defaultValue;
    }
    final Integer i = Ints.tryParse(value);
    if (i == null) {
      return defaultValue;
    }
    return i;
  }

  private int findRowIndexOfCell(final Element element) {
    for (final Element e : element.parents()) {
      if (e.tagName().equalsIgnoreCase("tr")) {
        return findRowIndexOfRow(e);
      }
    }
    return -1;
  }

  private int findRowIndexOfRow(final Element e) {
    // TODO: The best we can do without rowspan type info
    return e.siblingIndex();
  }

  private int findColIndexOfCell(final Element e) {
    // TODO: The best we can do without colspan type info
    return e.siblingIndex();
  }


  private void createAnchor(final JCas jCas, final AnnotationCollector collector,
      final Element element) {
    final String href = element.absUrl("href");
    if (href != null) {
      final Link l = new Link(jCas);
      l.setTarget(href);
      collector.add(l);

    } else {
      collector.add(new Anchor(jCas));
    }
  }

  private void createFromArticle(final JCas jCas, final AnnotationCollector collector,
      final Element element) {
    final String clazz = element.attr("class");
    switch (clazz.toLowerCase()) {
      case "sheet":
        collector.add(new Sheet(jCas));
        break;
      case "slide":
        collector.add(new Slide(jCas));
        break;
    }
  }

  private void createFromMain(final JCas jCas, final AnnotationCollector collector,
      final Element element) {
    final String clazz = element.attr("class");
    switch (clazz.toLowerCase()) {
      case "document":
        collector.add(new Document(jCas));
        break;
      case "spreadsheet":
        collector.add(new SpreadSheet(jCas));
        break;
      case "slideshow":
        collector.add(new SlideShow(jCas));
        break;
    }
  }

  private void createStyle(final JCas jCas, final AnnotationCollector collector,
      final String styleName) {
    final Style style = new Style(jCas);
    style.setDecoration(UimaTypesUtils.toArray(jCas, Collections.singleton(styleName)));
    collector.add(style);
    return;
  }

  private void createHeading(final JCas jCas, final AnnotationCollector collector,
      final int level) {
    final Heading h = new Heading(jCas);
    h.setLevel(level);
    collector.add(h);
  }

}
