package uk.gov.dstl.baleen.contentmappers;

import java.util.Collections;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
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

      // Table

      case "table":
        collector.add(new Table(jCas));
        break;

      case "thead":
        collector.add(new TableHeader(jCas));
        break;

      case "tbody":
        collector.add(new TableBody(jCas));
        break;

      case "tr":
        final TableRow tr = new TableRow(jCas);
        // TODO: Row index
        // tr.setRowIndex(findRowIndexOfRow(element));
        collector.add(tr);
        break;

      case "th":
        // fall through
      case "td":
        final TableCell td = new TableCell(jCas);
        td.setColumn(findColIndexOfCell(element));
        td.setRow(findRowIndexOfCell(element));
        collector.add(td);
        break;

      // Links and anchors

      case "a":
        createAnchor(jCas, collector, element);
        break;

      // Images

      case "img":
        collector.add(new Figure(jCas));
        break;


      // Styling

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

      // Purely structural

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

      // TOOD: Leftovers

      case "pre":
      case "blockquote":
      case "dl":
      case "q":
      case "dt":
      case "dd":
      case "address":
      case "ins":
      case "del":
        // TODO: Add schema types for (some of) these?
        return;

      default:
        return;
    }
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
    if (href == null) {
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
