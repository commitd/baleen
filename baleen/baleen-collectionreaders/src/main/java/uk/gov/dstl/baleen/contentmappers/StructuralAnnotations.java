package uk.gov.dstl.baleen.contentmappers;

import java.util.Collections;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
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
        collector.add(new TableRow(jCas));
        break;

      case "th":
        // fall through
      case "td":
        collector.add(new TableCell(jCas));
        // TODO: Row and Col index
        break;

      // Links and anchors

      case "a":
        // TODO: has href => Link?
        collector.add(new Anchor(jCas));
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
        createStyle(jCas, collector, "italics");
        break;

      case "b":
        createStyle(jCas, collector, "bold");
        break;

      // Purely structural

      case "div":
        // TODO: Means nothing.. could drop
        collector.add(new Section(jCas));
        break;

      case "span":
        // Do nothing
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
