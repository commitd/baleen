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
 * RecordDefinitions are regions surrounded by <<record:NAME:begin>> and
 * <<record:NAME:end>> marker text, where NAME is a user defined record type
 * name and must be consistent in the begin and end marker text. Each
 * RecordDefinition should cover one or more TemplateFieldDefinition
 * annotations.
 * </p>
 */
public class RecordDefinitionAnnotator extends BaleenAnnotator {

	public static final String DEFAULT_DATA_TYPE = "String";

	private static final String TEMPLATE_TOKEN_REGEX = "<<record:([A-Za-z0-9]+):begin>>(.*)<<record:\\1:end>>";

	private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile(TEMPLATE_TOKEN_REGEX, Pattern.DOTALL);

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		String documentText = jCas.getDocumentText();
		Matcher matcher = TEMPLATE_TOKEN_PATTERN.matcher(documentText);
		while (matcher.find()) {
			create(jCas, matcher);
		}
	}

	private void create(JCas jCas, Matcher matcher) {
		RecordDefinition field = new RecordDefinition(jCas);
		field.setName(matcher.group(1));
		field.setBegin(matcher.start(2));
		field.setEnd(matcher.end(2));
		field.setConfidence(1.0);
		jCas.addFsToIndexes(field);
	}

}
