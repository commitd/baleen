package uk.gov.dstl.baleen.contentmanipulators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;
import uk.gov.dstl.baleen.contentmanipulators.helpers.MarkupUtils;

/**
 * Creates HTML nodes which capture the paragraph classification markings.
 * 
 * If this manipulator sees (CLASSIFICATION) The rest of the paragraph. Then it removes the
 * CLASSIFICATION prefix and records the classification in the paragraph tag under data- tags. This
 * cleans up the text and allows a classification annotation to be added later.
 * 
 * This is a basic example, and may not work in all cases. It could be more robust.
 * 
 * NOTE this will only output classification tags if used in conjunction with the
 * DataAttributeMapper.
 */
public class ParagraphMarkedClassification implements ContentManipulator {

  private static final String CLASSFICATION_GROUP = "classfication";
  private static final Pattern PARAGRAPH_MARKING =
      Pattern.compile("^\\s*\\((?<" + CLASSFICATION_GROUP + ">.*?)\\).*");


  @Override
  public void manipulate(final Document document) {
    document.select("p").forEach(p -> {
      final String text = p.text();
      final Matcher matcher = PARAGRAPH_MARKING.matcher(text);
      if (matcher.find()) {

        final String classification = matcher.group(CLASSFICATION_GROUP);

        MarkupUtils.additionallyAnnotateAsType(p,
            "uk.gov.dstl.baleen.types.metadata.ProtectiveMarking");
        // TODO: We override this for simplicitiy but we could select the best classification etc
        // (or output eveyrthing later and let a cleander decide)
        MarkupUtils.setAttribute(p, "classification", classification.trim());

        // TODO: Ideally delete text the classification from the front. That need as a util as we
        // need to eat
        // up the children of p until we've got to the end. That's quite complex, you'd need to
        // spilt down the text nodes accross multiple children. We'll just remove the the first text
        // node matching the classifcation we've found as an interim.

        final String marking = "(" + classification + ')';
        for (final org.jsoup.nodes.TextNode t : p.textNodes()) {
          if (t.text().contains(marking)) {
            final String newText = t.text().replace(marking, "");
            t.text(newText);
          }
        }
      }
    });
  }


}
