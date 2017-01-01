//Dstl (c) Crown Copyright 2016
package uk.gov.dstl.baleen.annotators.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;
import uk.gov.dstl.baleen.annotators.misc.helpers.AbstractKeywordsAnnotator;
import uk.gov.dstl.baleen.annotators.misc.helpers.NoOpStemmer;

/**
 * Uses the RAKE (Rapid Automatic Keyword Extraction) algorithm to automatically
 * identify keywords in each document.
 * 
 * These keywords will be added as metadata to the document, and optionally can
 * also be added as Buzzwords
 * 
 * Based on the paper 'Automatic keyword extraction from individual documents' by 
 * Stuart Rose, Dave Engel, Nick Cramer and Wendy Cowley.
 * 
 * Optionally, you can choose to stem words prior to scoring, which will address
 * any variability in words caused by plurals, tense, etc.
 * This is an extension from the original paper. Essentially, the annotator maintains
 * a mapping between a stemmed version and the original version of the key phrase,
 * using the stemmed version for scoring and calculations, and then returning the
 * original version when required for output.
 * 
 * @baleen.javadoc
 */
public class RakeKeywords extends AbstractKeywordsAnnotator {
	
	/**
	 * The stemming algorithm to use, as defined in OpenNLP's SnowballStemmer.ALGORITHM enum, e.g. ENGLISH.
	 * If not set, or set to an undefined value, then no stemming will be used
	 * 
	 * @baleen.config
	 */
	public static final String PARAM_STEMMING = "stemming";
	@ConfigurationParameter(name = PARAM_STEMMING, defaultValue = "")
	protected String stemming;
	
	
	private Pattern stopwordPattern;
	private Stemmer stemmer;
	
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		
		if(!Strings.isNullOrEmpty(stemming)){
			try{
				final ALGORITHM algo = ALGORITHM.valueOf(stemming);
				stemmer = new SnowballStemmer(algo);
			}catch(final IllegalArgumentException iae){
				getMonitor().warn("Value of {} does not match pre-defined list, no stemming will be used.", PARAM_STEMMING, iae);
				stemmer = new NoOpStemmer();
			}
		}else{
			 stemmer = new NoOpStemmer();
		}
		
		stopwordPattern = buildStopwordsPattern();
	}
	
	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		final List<StemmedString> candidates = new ArrayList<>();
		
		//The definition of sentence as required by RAKE is different to that used by Baleen,
		//so we can't use the existing Sentence annotation.
		for(final String sentence : splitSentences(getTextInTextBlocks(jCas))){
			candidates.addAll(generateCandidates(sentence));
		}
		
		final Map<StemmedString, Double> scores = calculateScores(candidates);
		final Map<StemmedString, Double> keywords = generateKeywordScores(candidates, scores);
		
		final Multimap<Double, StemmedString> keywordsByValue = TreeMultimap.create();
		keywords.forEach((k, v) -> keywordsByValue.put(v, k));
		
		final Integer numKeywords = Integer.min(maxKeywords, keywords.size()/3);
		
		final List<Double> scoreValues = new ArrayList<>(keywordsByValue.keySet());
		Integer index = scoreValues.size() - 1;
		
		final List<StemmedString> finalKeywords = new ArrayList<>();
		while(finalKeywords.size() < numKeywords && index >= 0){
			finalKeywords.addAll(keywordsByValue.get(scoreValues.get(index)));
			index--;
		}
		
		final List<String> keywordsString = finalKeywords.stream().map(s -> s.getOriginalString()).collect(Collectors.toList());
		
		addKeywordsToJCas(jCas, keywordsString);
	}
	
	private List<StemmedString> generateCandidates(final String sentence){
		final String[] candidates = stopwordPattern.split(sentence);
	
		final List<StemmedString> normalizedCandidates = new ArrayList<>();
		
		for(final String c : candidates){
			if(c.trim().length() > 0){
				final String normalized = c.trim().toLowerCase();
				
				normalizedCandidates.add(new StemmedString(normalized, stemmer.stem(normalized)));
			}
		}
		
		return normalizedCandidates;
	}
	
	private Map<StemmedString, Double> calculateScores(final List<StemmedString> candidates){
		final Map<StemmedString, Integer> degree = new HashMap<>();
		final Map<StemmedString, Double> score = new HashMap<>();
		
		final Multiset<StemmedString> words = HashMultiset.create();
		
		for(final StemmedString candidate : candidates){
			final List<StemmedString> splitWords = splitCandidate(candidate);
			final Integer listDegree = splitWords.size();
			
			words.addAll(splitWords);
			
			for(final StemmedString word : splitWords){
				final int currDegree = degree.getOrDefault(word, 0);
				degree.put(word, currDegree + listDegree);
			}
		}
		
		for(final StemmedString word : words){
			score.put(word, degree.get(word) / (words.count(word) * 1.0));
		}
		
		return score;
	}
	
	private Map<StemmedString, Double> generateKeywordScores(final List<StemmedString> candidates, final Map<StemmedString, Double> scores){
		final Map<StemmedString, Double> keywords = new HashMap<>();
		
		for(final StemmedString candidate : candidates){
			final List<StemmedString> splitWords = splitCandidate(candidate);
			Double candidateScore = 0.0;
			
			for(final StemmedString word : splitWords){
				candidateScore += scores.getOrDefault(word, 0.0);
			}
			
			keywords.put(candidate, candidateScore);
		}
		
		return keywords;
	}
	
	private List<String> splitSentences(final String text){
		final String[] sentences = text.split("[-.!?,;:\\n\\t\\\"\\'\\(\\)\u2019\u2013\\\\\\/]");
		
		final List<String> returnedSentences = new ArrayList<>();
		for(final String sentence : sentences){
			if(sentence.trim().length() > 0){
				returnedSentences.add(sentence.trim().toLowerCase());
			}
		}
		
		return returnedSentences;
	}
	
	private List<StemmedString> splitCandidate(final StemmedString candidate){
		final String[] splitOrig = candidate.getOriginalString().split("\\s+");
		final String[] splitStemmed = candidate.getStemmedString().split("\\s+");
		
		final List<StemmedString> split = new ArrayList<>();
		
		for(int i = 0; i < splitOrig.length; i++){
			split.add(new StemmedString(splitOrig[i], splitStemmed[i]));
		}
		
		return split;
	}
	
}

/**
 * A class to hold two versions of a string in parallel - an original version and a stemmed version
 */
class StemmedString implements Comparable<StemmedString>{
	private final String strOrig;
	private final String strStemmed;
	
	/**
	 * Create a StemmedString from two strings
	 */
	public StemmedString(final String orig, final String stemmed){
		strOrig = orig;
		strStemmed = stemmed;
	}
	
	/**
	 * Create a StemmedString from one CharSequence (original) and one String (stemmed)
	 */
	public StemmedString(final CharSequence orig, final String stemmed){
		strOrig = orig.toString();
		strStemmed = stemmed;
	}
	/**
	 * Create a StemmedString from one CharSequence (stemmed) and one String (original)
	 */
	public StemmedString(final String orig, final CharSequence stemmed){
		strOrig = orig;
		strStemmed = stemmed.toString();
	}
	
	/**
	 * Create a StemmedString from two CharSequences
	 */
	public StemmedString(final CharSequence orig, final CharSequence stemmed){
		strOrig = orig.toString();
		strStemmed = stemmed.toString();
	}
	
	/**
	 * Get the original string
	 */
	public String getOriginalString(){
		return strOrig;
	}
	
	/**
	 * Get the stemmed string
	 */
	public String getStemmedString(){
		return strStemmed;
	}
	
	@Override
	public String toString(){
		return strStemmed;
	}

	@Override
	public int compareTo(final StemmedString s) {
		return strStemmed.compareTo(s.strStemmed);
	}
	
	@Override
	public boolean equals(final Object o){
		if(o instanceof StemmedString || o instanceof String){
			return strStemmed.equals(o.toString());
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		return strStemmed.hashCode();
	}
}