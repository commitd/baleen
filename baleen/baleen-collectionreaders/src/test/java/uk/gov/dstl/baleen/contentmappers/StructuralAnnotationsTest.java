package uk.gov.dstl.baleen.contentmappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import com.beust.jcommander.internal.Maps;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
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
import uk.gov.dstl.baleen.types.structure.Page;
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

public class StructuralAnnotationsTest {

  private final static Map<Tag, Class<?>[]> expected = Maps.newHashMap();

  static {
    addExpected("p", Paragraph.class);
    addExpected("h1", Heading.class);
    addExpected("h2", Heading.class);
    addExpected("h3", Heading.class);
    addExpected("h4", Heading.class);
    addExpected("h5", Heading.class);
    addExpected("h6", Heading.class);

    addExpected("ul", Unordered.class);
    addExpected("ol", Ordered.class);
    addExpected("li", ListItem.class);
    addExpected("dl", DefinitionList.class);
    addExpected("dt", DefinitionItem.class);
    addExpected("dd", DefinitionDescription.class);



    // Table

    addExpected("table", Table.class);

    addExpected("thead", TableHeader.class);

    addExpected("tfoot", TableFooter.class);

    addExpected("tbody", TableBody.class);
    addExpected("tr", TableRow.class);

    addExpected("th", TableCell.class);
    addExpected("td", TableCell.class);



    // Images

    addExpected("audio", Figure.class);
    addExpected("video", Figure.class);
    addExpected("embed", Figure.class);
    addExpected("object", Figure.class);
    addExpected("img", Figure.class);
    addExpected("map", Figure.class);
    addExpected("area", Figure.class);
    addExpected("canvas", Figure.class);
    addExpected("figure", Figure.class);


    addExpected("caption", Caption.class);
    addExpected("figcaption", Caption.class);

    // Styling

    addExpected("ins", Style.class);

    addExpected("i", Style.class);
    addExpected("em", Style.class);

    addExpected("b", Style.class);
    addExpected("strong", Style.class);

    addExpected("strike", Style.class);
    addExpected("s", Style.class);
    addExpected("del", Style.class);

    addExpected("sup", Style.class);
    addExpected("sub", Style.class);

    addExpected("small", Style.class);

    addExpected("big", Style.class);

    addExpected("mark", Style.class);

    // Purely structural

    addExpected("aside", Aside.class);
    addExpected("details", Details.class);
    addExpected("summary", Summary.class);
    addExpected("section", Section.class);
    addExpected("div", Section.class);



    addExpected("header", Header.class);
    addExpected("footer", Footer.class);
    addExpected("kbd", Preformatted.class);
    addExpected("samp", Preformatted.class);
    addExpected("code", Preformatted.class);
    addExpected("pre", Preformatted.class);
    addExpected("blockquote", Section.class, Quotation.class);

    addExpected("q", Quotation.class);

    addExpectedEmpty("span", "time", "meter", "dfn",
        "address",
        "abbr",
        "cite",
        "html",
        "head",
        "title",
        "meta",
        "base",
        "style",
        "script",
        "noscript",
        "link",
        "hr",
        "dialog",
        "nav",
        "menu",
        "menuitem",
        "param",
        "track",
        "source",
        "iframe",
        "form",
        "input",
        "textarea",
        "button",
        "select",
        "optgroup",
        "option",
        "label",
        "fieldset",
        "legend",
        "datalist",
        "keygen",
        "output",
        "ruby",
        "rt",
        "rp",
        "progress",
        "bdo",
        "bdi");


  }

  @Test
  public void testMap() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();

    final StructuralAnnotations sa = new StructuralAnnotations();

    for (final Map.Entry<Tag, Class<?>[]> e : expected.entrySet()) {
      final Element element = new Element(e.getKey(), "");
      final AnnotationCollector collector = new AnnotationCollector();
      sa.map(jCas, element, collector);

      final List<Annotation> annotations = collector.getAnnotations();

      final Class<?>[] classes = e.getValue();
      if (classes == null || classes.length == 0) {
        if (annotations != null) {
          assertTrue(annotations.isEmpty());
        }
      } else {
        assertEquals(annotations.size(), classes.length);
        for (int i = 0; i < classes.length; i++) {
          final Class<?> c = classes[i];
          assertTrue(c.isInstance(annotations.get(i)));
        }
      }

    }

  }



  @Test
  public void testAnchor() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final StructuralAnnotations sa = new StructuralAnnotations();

    final Element anchor = new Element(Tag.valueOf("a"), "");

    final AnnotationCollector collector = new AnnotationCollector();
    sa.map(jCas, anchor, collector);

    assertTrue(collector.getAnnotations().get(0) instanceof Anchor);
  }

  @Test
  public void testLink() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final StructuralAnnotations sa = new StructuralAnnotations();

    final Element anchor = new Element(Tag.valueOf("a"), "");
    anchor.attr("href", "http://example.com");

    final AnnotationCollector collector = new AnnotationCollector();
    sa.map(jCas, anchor, collector);

    assertTrue(collector.getAnnotations().get(0) instanceof Link);
  }

  @Test
  public void testMain() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final StructuralAnnotations sa = new StructuralAnnotations();

    final Map<String, Class<?>> expectedMain = new HashMap<>();
    expectedMain.put("Document", Document.class);
    expectedMain.put("SlideShow", SlideShow.class);
    expectedMain.put("SpreadSheet", SpreadSheet.class);
    expectedMain.put("Another", null);

    for (final Map.Entry<String, Class<?>> e : expectedMain.entrySet()) {
      final Element anchor = new Element(Tag.valueOf("main"), "");
      anchor.attr("class", e.getKey());

      final AnnotationCollector collector = new AnnotationCollector();
      sa.map(jCas, anchor, collector);

      if (e.getValue() != null) {
        assertTrue(e.getValue().isInstance(collector.getAnnotations().get(0)));
      } else {
        assertNull(collector.getAnnotations());
      }
    }
  }

  @Test
  public void testArticle() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();
    final StructuralAnnotations sa = new StructuralAnnotations();

    final Map<String, Class<?>> expectedArticle = new HashMap<>();
    expectedArticle.put("Sheet", Sheet.class);
    expectedArticle.put("Slide", Slide.class);
    expectedArticle.put("Page", Page.class);
    expectedArticle.put("Another", null);

    for (final Map.Entry<String, Class<?>> e : expectedArticle.entrySet()) {
      final Element anchor = new Element(Tag.valueOf("article"), "");
      anchor.attr("class", e.getKey());

      final AnnotationCollector collector = new AnnotationCollector();
      sa.map(jCas, anchor, collector);

      if (e.getValue() != null) {
        assertTrue(e.getValue().isInstance(collector.getAnnotations().get(0)));
      } else {
        assertNull(collector.getAnnotations());
      }
    }
  }

  private static void addExpected(final String tagName, final Class<?>... classes) {
    expected.put(Tag.valueOf(tagName), classes);
  }

  private static void addExpectedEmpty(final String... tagNames) {
    Arrays.stream(tagNames).forEach(s -> addExpected(s));

  }
}
