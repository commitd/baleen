//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.ComparableTextSpan;
import uk.gov.dstl.baleen.uima.data.TextBlock;


/**
 * Find British Army Units using Regex
 * 
 * <p>A series of Regex are used to find British Army units (e.g. Platoons, Companies) and then they are aggregated based on hierarchy where possible.</p>
 * 
 * 
 */
public class BritishArmyUnits extends BaleenTextAwareAnnotator {
	private final Pattern section = Pattern.compile("\\b\\d+ Sect\\b");
	private final Pattern platoon = Pattern.compile("\\b\\d+ Pl\\b");
	private final Pattern company = Pattern.compile("\\b[A-Z] Coy\\b");
	
	private static final int HIERARCHY_SECTION = 1;
	private static final int HIERARCHY_PLATOON = 2;
	private static final int HIERARCHY_COMPANY = 3;
	
	@Override
	public void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		
		final String documentText = block.getCoveredText();
	
		// 1. Find all sections, platoons, companies, etc.

		final List<ComparableTextSpan> sectionSpans = ComparableTextSpan.buildSpans(documentText, section);
		final List<ComparableTextSpan> platoonSpans = ComparableTextSpan.buildSpans(documentText, platoon);
		final List<ComparableTextSpan> companySpans = ComparableTextSpan.buildSpans(documentText, company);
		
		final Map<Integer, List<ComparableTextSpan>> hierarchySpans = new HashMap<>();
	
		hierarchySpans.put(HIERARCHY_SECTION, sectionSpans);
		hierarchySpans.put(HIERARCHY_PLATOON, platoonSpans);
		hierarchySpans.put(HIERARCHY_COMPANY, companySpans);
	
		
		// 2. Merge when spans are separated by a space or a comma, and the second span is higher in the hierarchy
		final SortedSet<Integer> hierarchyLevels = new TreeSet<>(hierarchySpans.keySet());
		for(Integer level = hierarchyLevels.first(); level < hierarchyLevels.last(); level++){
			compareHierarchy(documentText, hierarchySpans, level);
		}
		
		// 3. Add spans to JCas as organisations
		for(final ComparableTextSpan span : hierarchySpans.get(hierarchyLevels.last())){
			final Organisation org = new Organisation(block.getJCas());
			
			org.setConfidence(1.0);
			block.setBeginAndEnd(org, span.getStart(), span.getEnd());
			org.setValue(span.getValue());

			addToJCasIndex(org);
		}
	}
	
	private void compareHierarchy(final String documentText, final Map<Integer, List<ComparableTextSpan>> hierarchySpans, final int level) {
		
		List<ComparableTextSpan> newSpans = hierarchySpans.get(level + 1);
		if(newSpans == null)
			newSpans = new ArrayList<>();
		
		for(final ComparableTextSpan s1 : hierarchySpans.get(level)){
			ComparableTextSpan s = s1;
			for(final ComparableTextSpan s2 : hierarchySpans.get(level + 1)){
				final ComparableTextSpan t = mergeSpansIfPossible(s1, s2, documentText);
				if(t != null){
					s = t;
					newSpans.remove(s2);
					break;
				}
			}
			
			newSpans.add(s);
		}
		
		hierarchySpans.put(level + 1, newSpans);
	}
	
	private ComparableTextSpan mergeSpansIfPossible(final ComparableTextSpan s1, final ComparableTextSpan s2, final String documentText) {
		if(s1.getStart() < s2.getEnd()) {
			final String text = documentText.substring(s1.getStart(), s2.getEnd());
			if(text.equals(s1.getValue() + " " + s2.getValue()) || text.equals(s1.getValue() + "," + s2.getValue()) || text.equals(s1.getValue() + ", " + s2.getValue())){
				return new ComparableTextSpan(s1.getStart(), s2.getEnd(), text);
			}
		}
		
		return null;
	}
}
