// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.consumers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import uk.gov.dstl.baleen.consumers.helpers.AbstractHtml;
import uk.gov.dstl.baleen.types.semantic.Entity;

/**
 * Creates HTML5 versions of the document, with entities annotated as spans. The original formatting
 * of the document is lost, and only the content is kept.
 *
 * Relationships are not currently supported.
 *
 *
 * @baleen.javadoc
 */
public class Html5 extends AbstractHtml {


  private Map<Integer, String> getEntityInsertPositions(final JCas jCas) {
    final Map<Integer, String> insertPositions = new TreeMap<>();
    final Map<Integer, List<Entity>> entityStartPositions = new HashMap<>();
    for (final Entity e : JCasUtil.select(jCas, Entity.class)) {
      if (insertPositions.containsKey(e.getBegin())) {
        final List<Entity> entities =
            entityStartPositions.getOrDefault(e.getBegin(), new ArrayList<>());

        final long eCount = entities.stream().filter(e2 -> e2.getEnd() > e.getEnd()).count();

        final String[] spans = insertPositions.get(e.getBegin()).split("(?<=>)");
        insertPositions.put(e.getBegin(), joinSpans(eCount, e, spans));
      } else {
        insertPositions.put(e.getBegin(), generateSpanStart(e));
      }

      final List<Entity> entities =
          entityStartPositions.getOrDefault(e.getBegin(), new ArrayList<>());
      entities.add(e);
      entityStartPositions.put(e.getBegin(), entities);

      String end = insertPositions.getOrDefault(e.getEnd(), "");
      end = "</span>" + end;
      insertPositions.put(e.getEnd(), end);
    }

    return insertPositions;
  }

  /**
   * @param eCount The number of entities starting in the same position as e, but finishing
   *        afterwards
   * @param e The entity of interest
   * @param spans The array of spans that we already have
   * @return
   */
  private String joinSpans(final long eCount, final Entity e, final String[] spans) {
    final StringBuilder joinedSpans = new StringBuilder(eCount == 0 ? generateSpanStart(e) : "");

    Integer i = 0;
    for (final String span : spans) {
      joinedSpans.append(span);
      i++;

      if (i == eCount) {
        joinedSpans.append(generateSpanStart(e));
      }
    }

    return joinedSpans.toString();
  }

  private String generateSpanStart(final Entity e) {
    final String value = e.getValue() == null ? "" : e.getValue().replaceAll("\"", "'");
    final String referent =
        e.getReferent() == null ? "" : Long.toString(e.getReferent().getInternalId());

    return String.format("<span class=\"baleen %s\" id=\"%s\" value=\"%s\" data-referent=\"%s\" >",
        e.getClass().getSimpleName(),
        e.getExternalId(), value, referent);
  }

  @Override
  protected void writeBody(final JCas jCas, final Element body) {
    // Entities
    final Map<Integer, String> insertPositions = getEntityInsertPositions(jCas);

    String text = jCas.getDocumentText();
    Integer offset = 0;
    for (final Entry<Integer, String> pos : insertPositions.entrySet()) {
      final String insert = pos.getValue();
      text =
          text.substring(0, pos.getKey() + offset) + insert + text.substring(pos.getKey() + offset);
      offset += insert.length();
    }

    for (final String para : text.split("[\n]+")) {
      final Document docFragment = Jsoup.parseBodyFragment(para);
      final Element p = body.appendElement("p");
      for (final Node n : docFragment.body().childNodes()) {
        p.appendChild(n.clone());
      }
    }
  }
}
