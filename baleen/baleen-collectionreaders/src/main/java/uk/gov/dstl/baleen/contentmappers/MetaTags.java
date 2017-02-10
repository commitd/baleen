package uk.gov.dstl.baleen.contentmappers;

import org.apache.uima.jcas.JCas;
import org.jsoup.nodes.Element;

import com.google.common.base.Strings;

import uk.gov.dstl.baleen.contentmappers.helpers.AnnotationCollector;
import uk.gov.dstl.baleen.contentmappers.helpers.ContentMapper;
import uk.gov.dstl.baleen.types.metadata.Metadata;

/**
 * Convert from meta tags into a Metadata annotation, retaining the key (name) and value (content or
 * charset) attributes.
 */
public class MetaTags implements ContentMapper {

  @Override
  public void map(final JCas jCas, final Element element, final AnnotationCollector collector) {
    switch (element.tagName().toLowerCase()) {

      case "meta":
        final Metadata md = new Metadata(jCas);

        final String name = element.attr("name");
        md.setKey(name);

        final String content = element.attr("content");
        final String charset = element.attr("charset");
        if (!Strings.isNullOrEmpty(content)) {
          md.setValue(content);
        } else if (!Strings.isNullOrEmpty(charset)) {
          md.setValue(charset);
        }

        collector.add(md);
        break;


      default:
        return;
    }
  }

}
