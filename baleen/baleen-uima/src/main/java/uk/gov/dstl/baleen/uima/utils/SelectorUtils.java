package uk.gov.dstl.baleen.uima.utils;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.BaleenAnnotation;
import uk.gov.dstl.baleen.types.structure.Structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectorUtils {

	private static final String NTH_OF_TYPE_REGEX = "nth-of-type\\((\\d+)\\)";

	private static final Pattern NTH_OF_TYPE_PATTERN = Pattern.compile(NTH_OF_TYPE_REGEX);

	private static List<SelectorPart> parseSelector(String value, String... packages) throws InvalidParameterException {
		List<SelectorPart> selectorParts = new ArrayList<>();
		String[] parts = value.split("\\s*>\\s*");
		for (String part : parts) {
			int colon = part.indexOf(":");
			if (colon != -1) {
				String[] typeAndQualifier = part.split(":");
				selectorParts.add(new SelectorPart(getType(typeAndQualifier[0], packages), typeAndQualifier[1]));
			} else {
				selectorParts.add(new SelectorPart(getType(part, packages)));
			}
		}
		return selectorParts;
	}

	/**
	 * Given a selector string and a JCas instance, returns Structure elements
	 * from the JCas that match the selector.
	 *
	 * <p>
	 * If the varargs packages parameter is provided, the element names in the
	 * selector are looked up in the specified packages in order until a match
	 * is found. If it is not provided, the selector elements are assumed to be
	 * fully qualified class names - <strong>note: this behaviour may
	 * change</strong>)
	 * </p>
	 *
	 * @param jCas
	 *            the jCas to search
	 * @param selectorString
	 *            the selector string
	 * @param packages
	 *            optional varargs/array of packages to resolve element names
	 *            against
	 * @return a List of structure elements from the JCas that match the given
	 *         selector string
	 * @throws InvalidParameterException
	 *             if one of the selector element names cannot be resolved to a
	 *             type.
	 */
	public static List<? extends Structure> select(JCas jCas, String selectorString, String... packages)
			throws InvalidParameterException {
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

	private static Class<Structure> getType(String typeName, String[] packages) throws InvalidParameterException {
		return CpeBuilderUtils.getClassFromString(typeName, packages);
	}

	static class SelectorPart {

		Class<Structure> type;

		String psuedoSelector;

		SelectorPart(Class<Structure> type) {
			this.type = type;
		}

		SelectorPart(Class<Structure> type, String psuedoSelector) {
			this.type = type;
			this.psuedoSelector = psuedoSelector;
		}
	}

	public static String generatePath(BaleenAnnotation parent, BaleenAnnotation child,
			Set<Class<? extends Structure>> structuralClasses) {
		String parentPath = generatePath(parent, structuralClasses);
		String childPath = generatePath(child, structuralClasses);
		return childPath.replace(parentPath, "").trim();
	}

	public static String generatePath(final BaleenAnnotation templateField,
			Set<Class<? extends Structure>> structuralClasses) {
		StringBuilder sb = new StringBuilder();
		List<Structure> covering = JCasUtil.selectCovering(Structure.class, templateField);
		ListIterator<Structure> iterator = covering.listIterator();
		Structure previous = null;
		while (iterator.hasNext()) {
			previous = iterator.next();
			if (structuralClasses.contains(previous.getClass())) {
				break;
			}
			previous = null;
		}

		if (previous != null) {
			sb.append(previous.getType().getShortName());
			List<? extends Structure> previousOfType = JCasUtil.selectPreceding(previous.getClass(), previous,
					Integer.MAX_VALUE);
			long previousDepth = previous.getDepth();
			long sameTypeSameDepthPreceedingCount = previousOfType.stream().filter(s -> s.getDepth() == previousDepth)
					.count();
			if (sameTypeSameDepthPreceedingCount > 1) {
				sb.append(":nth-of-type(");
				sb.append(sameTypeSameDepthPreceedingCount + 1);
				sb.append(")");
			}

			while (iterator.hasNext()) {
				Structure next = iterator.next();
				if (!structuralClasses.contains(next.getClass())) {
					continue;
				}
				List<Structure> children = JCasUtil.selectCovered(getStructureClass(next), previous);
				int count = 0;
				for (Structure child : children) {
					if (child.getDepth() == previous.getDepth() + 1) {
						count++;
					}
					if (child.getCoveredText().contains(templateField.getCoveredText())) {
						sb.append(" > ");
						sb.append(next.getType().getShortName());
						if (children.size() > 1) {
							sb.append(":nth-of-type(");
							sb.append(count);
							sb.append(")");
						}
						break;
					}
				}
				previous = next;
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private static Class<Structure> getStructureClass(Structure type) {
		return (Class<Structure>) type.getClass();
	}

}
