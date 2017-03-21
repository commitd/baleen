package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.types.templates.RecordDefinition;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotates record definitions found in documents using a regular expression.
 * <p>
 * RecordDefinitions are regions surrounded by &lt;&lt;record:NAME:begin&gt;&gt;
 * and &lt;&lt;record:NAME:end&gt;&gt; marker text, where NAME is a user defined
 * record type name and must be consistent in the begin and end marker text.
 * Each RecordDefinition should cover one or more TemplateFieldDefinition
 * annotations to be useful downstream.
 * </p>
 * <p>
 * This annotator should be used in conjuction with
 * {@link TemplateFieldDefinitionAnnotator}.
 * </p>
 */
public class RecordDefinitionAnnotator extends BaleenAnnotator {

	/** Regular expression used to match records. */
	private static final String RECORD_TOKEN_REGEX = "<<record:([A-Za-z0-9]+):begin>>(.*?)<<record:\\1:end>>";

	/**
	 * The compiled regular expression - compiled with the DOTALL option
	 * (effectively '(?s)' in the regex) to enable matches over multiple lines.
	 */
	private static final Pattern RECORD_TOKEN_PATTERN = Pattern.compile(RECORD_TOKEN_REGEX, Pattern.DOTALL);

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		String documentText = jCas.getDocumentText();
		Matcher matcher = RECORD_TOKEN_PATTERN.matcher(documentText);
		while (matcher.find()) {
			createRecordDefinitionAnnotation(jCas, matcher);
		}
	}

	/**
	 * Creates a record definition annotation and adds it to the JCas indexes.
	 *
	 * @param jCas
	 *            the JCas
	 * @param matcher
	 *            the matcher that triggered the creation, which must have two
	 *            groups (first being the name, and the second being the content
	 *            within the record)
	 */
	private void createRecordDefinitionAnnotation(JCas jCas, Matcher matcher) {
		RecordDefinition recordDefinition = new RecordDefinition(jCas);
		recordDefinition.setName(matcher.group(1));
		recordDefinition.setBegin(matcher.start(2));
		recordDefinition.setEnd(matcher.end(2));
		recordDefinition.setConfidence(1.0);
		addToJCasIndex(recordDefinition);
	}

}
