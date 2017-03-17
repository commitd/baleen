package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.uima.data.TextBlock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotates records found in documents using a regular expression.
 * <p>
 * Records are regions surrounded by <<record:NAME:start>> and <<record:NAME:end>> marker text,
 * where NAME is a user defined record type name.
 * </p>
 */
public class RecordDefinitionAnnotator extends AbstractRegexAnnotator<Record> {

  public static final String DEFAULT_DATA_TYPE = "String";

  private static final String TEMPLATE_TOKEN_REGEX = "<<record:([A-Za-z0-9]+):(begin|end)>>";

  private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile(TEMPLATE_TOKEN_REGEX);

  public RecordDefinitionAnnotator() {
    super(TEMPLATE_TOKEN_PATTERN, 1.0);
  }
	public void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		final String text = block.getCoveredText();
super.doProcessTextBlock(block);
	}
  @Override
  protected Record create(JCas jCas, Matcher matcher) {
    Record field = new Record(jCas);
    field.setName(matcher.group(1));
    field.setMarker(matcher.group(2));
    return field;
  }

}
