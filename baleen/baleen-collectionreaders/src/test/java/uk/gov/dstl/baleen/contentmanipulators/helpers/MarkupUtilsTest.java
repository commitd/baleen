package uk.gov.dstl.baleen.contentmanipulators.helpers;

import static org.junit.Assert.assertEquals;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

public class MarkupUtilsTest {

  @Test
  public void testAdditionallyAnnotateAsType() {
    final Element e = new Element(Tag.valueOf("p"), "");
    MarkupUtils.additionallyAnnotateAsType(e, "testtype");

    assertEquals(MarkupUtils.getAttribute(e, "types"), "testtype");

  }

  @Test
  public void testSetAttribute() {
    final Element e = new Element(Tag.valueOf("p"), "");
    MarkupUtils.setAttribute(e, "key", "value");
    assert (MarkupUtils.getAttribute(e, "key").equals("value"));

    MarkupUtils.setAttribute(e, "key", "value2");
    assert (MarkupUtils.getAttribute(e, "key").equals("value2"));

  }

  @Test
  public void testAddAttribute() {
    final Element e = new Element(Tag.valueOf("p"), "");
    MarkupUtils.addAttribute(e, "key", "value1");

    assert (MarkupUtils.getAttribute(e, "key").contains("value1"));

    MarkupUtils.addAttribute(e, "key", "value2");

    assert (MarkupUtils.getAttribute(e, "key").contains("value1"));
    assert (MarkupUtils.getAttribute(e, "key").contains("value2"));

    assert (MarkupUtils.getAttributes(e, "key").contains("value1"));
    assert (MarkupUtils.getAttributes(e, "key").contains("value2"));
  }



}
