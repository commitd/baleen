//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import uk.gov.dstl.baleen.annotators.helpers.QuantityUtils;
import uk.gov.dstl.baleen.types.common.Quantity;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;

/**
 * Annotate weights within a document using regular expressions
 * 
 * <p>The document content is searched for things that might represent weights using regular expressions.
 * Any extracted weights are normalized to KG.</p>
 * 
 * 
 */
public class Weight extends BaleenTextAwareAnnotator {
	public static final double LONG_TON_TO_KG = 1016.0469088;
	public static final double STONE_TO_KG = 6.35029318;
	public static final double POUNDS_TO_KG = 0.45359237;
	public static final double OUNCES_TO_KG = 0.028349523125;
	
	private final Pattern tonnePattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(tonne)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern kgPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(kg|kilogram|kilo)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern gPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(g|gram)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern mgPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(mg|milligram)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern tonPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(ton)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern lbPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(lb)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern stonePattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(st|stone)(s)?\\b", Pattern.CASE_INSENSITIVE);
	private final Pattern ozPattern = Pattern.compile("\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(oz|ounce)(s)?\\b", Pattern.CASE_INSENSITIVE);
	
	@Override
	public void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		final String text = block.getCoveredText();
		
		process(block, text, tonnePattern, "tonne", 1000);
		process(block, text, kgPattern, "kg", 1.0);
		process(block, text, gPattern, "g", 1.0/1000.0);
		process(block, text, mgPattern, "mg", 1.0/1000000.0);
		process(block, text, tonPattern, "long ton", LONG_TON_TO_KG);
		process(block, text, stonePattern, "st", STONE_TO_KG);
		process(block, text, lbPattern, "lb", POUNDS_TO_KG);
		process(block, text, ozPattern, "oz", OUNCES_TO_KG);

	}
	
	private void process(final TextBlock block, final String text, final Pattern pattern, final String unit, final double scale) {
		final Matcher matcher = pattern.matcher(text);
		while(matcher.find()){
			addQuantity(block, matcher, unit, scale);
		}
	}
	
	private void addQuantity(final TextBlock block, final Matcher matcher, final String unit,
			final double scale) {
		final Quantity quantity = QuantityUtils.createQuantity(block.getJCas(), matcher, unit, scale, "kg", "weight");
		if(quantity != null) {
		    block.setBeginAndEnd(quantity, quantity.getBegin(), quantity.getEnd());
			addToJCasIndex(quantity);
		}
	}
}
