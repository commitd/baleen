package uk.gov.dstl.baleen.uima.utils;

import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.BaleenAnnotation;
import uk.gov.dstl.baleen.types.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.OptionalInt;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
		List<Structure> result = new ArrayList<>();

		OptionalInt min = JCasUtil.select(jCas, Structure.class).stream().mapToInt(Structure::getDepth).min();
		if (min.isPresent() && !selectorParts.isEmpty()) {
			int depth = min.getAsInt();
			SelectorPart selectorPart = selectorParts.get(0);
			List<Structure> parents = JCasUtil
					.selectCovered(selectorPart.type, (AnnotationFS) jCas.getDocumentAnnotationFs()).stream()
					.filter(s -> depth == s.getDepth()).collect(Collectors.toList());
			select(selectorParts.subList(1, selectorParts.size()), selectorPart, result, depth + 1, parents);
		}

		return result;
	}

	private static void select(List<SelectorPart> remainingParts, SelectorPart selectorPart, List<Structure> result,
			int depth, List<Structure> parents) {
		// there are no further parts to examine, so add all of the parent
		// candidates to the result
		if (remainingParts.isEmpty()) {
			if (selectorPart.psuedoSelector != null) {
				Matcher matcher = NTH_OF_TYPE_PATTERN.matcher(selectorPart.psuedoSelector);
				if (matcher.matches()) {
					int nth = Integer.parseInt(matcher.group(1));
					Structure nthOfChild = nthOfChild(parents, nth);
					if (nthOfChild != null) {
						result.add(nthOfChild);
					}
				}
			} else {
				result.addAll(parents);
			}
		} else {
			if (selectorPart.psuedoSelector != null) {
				Matcher matcher = NTH_OF_TYPE_PATTERN.matcher(selectorPart.psuedoSelector);
				if (matcher.matches()) {
					int nth = Integer.parseInt(matcher.group(1));
					SelectorPart newSelectorPart = remainingParts.get(0);
					Structure nthOfChild = nthOfChild(parents, nth);
					if (nthOfChild == null) {
						return;
					}
					List<Structure> newParents = JCasUtil.selectCovered(newSelectorPart.type, nthOfChild).stream()
							.filter(s -> depth == s.getDepth()).collect(Collectors.toList());
					select(remainingParts.subList(1, remainingParts.size()), newSelectorPart, result, depth + 1,
							newParents);
				}
			} else {
				for (Structure structure : parents) {
					SelectorPart newSelectorPart = remainingParts.get(0);
					List<Structure> newParents = JCasUtil.selectCovered(newSelectorPart.type, structure).stream()
							.filter(s -> depth == s.getDepth()).collect(Collectors.toList());
					select(remainingParts.subList(1, remainingParts.size()), newSelectorPart, result, depth + 1,
							newParents);
				}
			}
		}
	}

	private static Structure nthOfChild(List<Structure> structures, int n) {
		if (n > structures.size()) {
			return null;
		}
		return structures.get(n - 1);
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

	public static String generatePath(JCas jcas, BaleenAnnotation parent, BaleenAnnotation child,
			Set<Class<? extends Structure>> structuralClasses) {
		String parentPath = generatePath(jcas, parent, structuralClasses);
		String childPath = generatePath(jcas, child, structuralClasses);
		return childPath.replace(parentPath, "").trim();
	}

	public static String generatePath(final JCas jCas, final BaleenAnnotation templateField,
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
			long previousDepth = previous.getDepth();
			long countOfType = JCasUtil.select(jCas, previous.getClass()).stream()
					.filter(s -> s.getDepth() == previousDepth).count();
			if (countOfType > 1) {
				List<? extends Structure> previousOfType = JCasUtil.selectPreceding(previous.getClass(), previous,
						Integer.MAX_VALUE);
				long sameTypeSameDepthPreceedingCount = previousOfType.stream()
						.filter(s -> s.getDepth() == previousDepth).count();
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
