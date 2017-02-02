package uk.gov.dstl.baleen.uima.utils;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectorUtils {

  private static final String NTH_OF_TYPE_REGEX = "nth-of-type\\((\\d+)\\)";

  private static final Pattern NTH_OF_TYPE_PATTERN = Pattern.compile(NTH_OF_TYPE_REGEX);

  private static List<SelectorPart> parseSelector(String value, String... packages)
      throws InvalidParameterException {
    List<SelectorPart> selectorParts = new ArrayList<>();
    String[] parts = value.split("\\s*>\\s*");
    for (String part : parts) {
      int colon = part.indexOf(":");
      if (colon != -1) {
        String[] typeAndQualifier = part.split(":");
        selectorParts
            .add(new SelectorPart(getType(typeAndQualifier[0], packages), typeAndQualifier[1]));
      } else {
        selectorParts.add(new SelectorPart(getType(part, packages)));
      }
    }
    return selectorParts;
  }

  public static List<? extends Structure> select(JCas jCas, String selectorString,
      String... packages) throws InvalidParameterException {
    List<SelectorPart> selectorParts = parseSelector(selectorString, packages);
    Iterator<SelectorPart> iterator = selectorParts.iterator();

    if (iterator.hasNext()) {
      SelectorPart selectorPart = iterator.next();
      List<Structure> candidates = JCasUtil.selectCovered(jCas, selectorPart.type, 0,
          jCas.getDocumentText().length());
      while (iterator.hasNext()) {
        List<Structure> newCandidates = new ArrayList<>();
        selectorPart = iterator.next();
        for (Structure structure : candidates) {
          List<Structure> covered = JCasUtil.selectCovered(selectorPart.type, structure);
          if (selectorPart.psuedoSelector != null) {
            Matcher matcher = NTH_OF_TYPE_PATTERN.matcher(selectorPart.psuedoSelector);
            if (matcher.matches()) {
              int nth = Integer.parseInt(matcher.group(1));
              int parentDepth = structure.getDepth();
              int count = 0;
              for (Structure child : covered) {
                if (child.getDepth() == parentDepth + 1) {
                  count++;
                }
                if (count == nth) {
                  newCandidates.add(child);
                  break;
                }
              }
            }
          } else {
            newCandidates.addAll(covered);
          }
        }
        candidates = newCandidates;
        if (!newCandidates.isEmpty() && !iterator.hasNext()) {
          return candidates;
        }
      }
    }
    return Collections.emptyList();
  }

  private static Class<Structure> getType(String typeName, String[] packages)
      throws InvalidParameterException {
    return CpeBuilderUtils.getClassFromString(typeName, packages);
  }

  private static class SelectorPart {

    private Class<Structure> type;

    private String psuedoSelector;

    private SelectorPart(Class<Structure> type) {
      this.type = type;
    }

    private SelectorPart(Class<Structure> type, String psuedoSelector) {
      this.type = type;
      this.psuedoSelector = psuedoSelector;
    }
  }
}
