package uk.gov.dstl.baleen.contentmanipulators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;
import uk.gov.dstl.baleen.contentmanipulators.helpers.MarkupUtils;

public class ParagraphMarkedClassification implements ContentManipulator {

  private static final String CLASSFICATION_GROUP = "classfication";
  private static final Pattern PARAGRAPH_MARKING =
      Pattern.compile("^\\s*\\(?<" + CLASSFICATION_GROUP + ">.*?\\).*");


  @Override
  public void manipulate(final Document document) {
    document.select("p").forEach(p -> {
      final String text = p.text();
      final Matcher matcher = PARAGRAPH_MARKING.matcher(text);
      if (matcher.matches()) {
        final String classification = matcher.group(CLASSFICATION_GROUP);

        MarkupUtils.additionallyAnnotateAsType(p, "Classification");
        // TODO: We override this for simplicitiy but we could select the best classification etc
        // (or output eveyrthing later and let a cleander decide)
        MarkupUtils.setAttribute(p, "classification", classification);

        // TODO: p delete the classification from the front. That need as a util as we need to eat
        // up the children of p until we've got to the end
      }
    });
  }


}
