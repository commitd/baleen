package uk.gov.dstl.baleen.contentmanipulators;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import uk.gov.dstl.baleen.contentmanipulators.helpers.ContentManipulator;

public class NewLineToNewParagraph implements ContentManipulator {

  @Override
  public void manipulate(final Document document) {

    // Find elements which need to be spilt up
    final Set<Element> elementsWithBr = new HashSet<>();
    document.select("br").forEach(e -> elementsWithBr.add(e.parent()));

    // For each parent
    elementsWithBr.forEach(e -> {
      final List<Element> runs = collectRuns(document, e);
      if (!runs.isEmpty()) {
        addRunsToDom(e, runs);
      }
    });
  }

  private List<Element> collectRuns(final Document document, final Element e) {
    final List<Element> runs = new LinkedList<>();
    Element run = null;
    for (final Node c : e.childNodesCopy()) {

      if (c instanceof Element && ((Element) c).tagName().equalsIgnoreCase("br")) {
        // If we hit a br then add the old run and start a new one
        if (run != null) {
          runs.add(run);
          run = null;
        }
      } else {
        // If not a br then add this node to the other
        if (run == null) {
          run = document.createElement("p");
        }
        run.appendChild(c);
      }
    }

    // Add the last run
    if (run != null) {
      runs.add(run);
    }

    return runs;
  }

  private void addRunsToDom(final Element e, final List<Element> runs) {
    // Add these new spans into the DOM
    if (e.tagName().equalsIgnoreCase("p")) {
      // If this is a p, then just add below it
      // reverse order so the first element of runs ends up closest to p as it should be
      Collections.reverse(runs);
      runs.forEach(e::after);
      // Delete the old paragraph
      e.remove();
    } else {
      // If we aren't in a p (eg in a li) then lets add paragraphs to this element
      // But first clear it out
      e.children().remove();
      runs.forEach(e::appendChild);
    }
  }

}
