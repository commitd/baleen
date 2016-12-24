package uk.gov.dstl.baleen.contentmanipulators.helpers;

import java.util.List;

import org.jsoup.nodes.Element;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public final class MarkupUtils {
  private static final String BALEEN_ATTRIBUTE_PREFIX = "data-baleen-";
  private static final String ANNOTATION_TYPE_ATTRIBUTE = "types";

  public static final String ATTRIBUTE_VALUE_SEPARATOR = ";";
  private static final Joiner ATTRIBUTE_VALUE_JOINER =
      Joiner.on(ATTRIBUTE_VALUE_SEPARATOR).skipNulls();
  private static final Splitter ATTRIBUTE_VALUE_SPLITTER =
      Splitter.on(ATTRIBUTE_VALUE_SEPARATOR).trimResults().omitEmptyStrings();

  private MarkupUtils() {
    // Singleton
  }

  public static void additionallyAnnotateAsType(final Element e, final String type) {
    addAttribute(e, ANNOTATION_TYPE_ATTRIBUTE, type);
  }

  public static void setAttribute(final Element e, final String key, final String value) {
    e.attr(attributeKey(key), value);
  }

  public static void addAttribute(final Element e, final String key, final String value) {
    final String fullKey = attributeKey(key);
    String current = e.attr(fullKey);
    if (Strings.isNullOrEmpty(current)) {
      current = value;
    } else {
      current = concatenateAttribute(current, value);
    }
    e.attr(fullKey, current);
  }

  private static String attributeKey(final String key) {
    return BALEEN_ATTRIBUTE_PREFIX + key;
  }

  private static String concatenateAttribute(final String... values) {
    return ATTRIBUTE_VALUE_JOINER.join(values);
  }

  public static String getAttribute(final Element e, final String key) {
    final String fullKey = attributeKey(key);
    return e.attr(fullKey);
  }

  public static List<String> getAttributes(final Element e, final String key) {
    return ATTRIBUTE_VALUE_SPLITTER.splitToList(getAttribute(e, key));
  }
}
