//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators.regex.internals;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.resources.SharedCountryResource;
import uk.gov.dstl.baleen.types.common.Nationality;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;

/**
 * Extract nationality demonyms, e.g. French, from text
 * 
 * <p>A list of demonyms are loaded from a JSON file, and each is searched for in the text. If found, then an annotation is created.</p>
 * 
 * 
 */
public class NationalityRegex extends BaleenTextAwareAnnotator {
	/**
	 * Connection to Country Resource
	 * 
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedCountryResource
	 */
	public static final String KEY_COUNTRY = "country";
	@ExternalResource(key = KEY_COUNTRY)
	private SharedCountryResource country;

	private final Map<String, Pattern> countryPatterns = new HashMap<>();
	
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		for(final Entry<String, String> e : country.getDemonyms().entrySet()){
			final Pattern p = Pattern.compile("\\b" + e.getKey() + "\\b", Pattern.CASE_INSENSITIVE);
			countryPatterns.put(e.getValue(), p);
		}
	}
	
	@Override
	public void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		final String text = block.getCoveredText();

		for(final Entry<String, Pattern> e : countryPatterns.entrySet()){
			final Matcher matcher = e.getValue().matcher(text);
			
			while(matcher.find()){
				getMonitor().debug("Found nationality '{}' in text", matcher.group(0));
				
				final Nationality n = new Nationality(block.getJCas());
				
				n.setConfidence(1.0f);
				block.setBeginAndEnd(n, matcher.start(), matcher.end());
				n.setValue(matcher.group(0));
				
				n.setCountryCode(e.getKey());
				
				addToJCasIndex(n);
			}
		}
	}
}
