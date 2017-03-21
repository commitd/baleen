package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotates template fields found in documents using a regular expression.
 * <p>
 * Template fields are text surrounded by ASCII double angle brackets, eg
 * &lt;&lt;field:fieldname&gt;&gt; for the field "fieldname".
 * </p>
 */
public class TemplateFieldDefinitionAnnotator extends AbstractRegexAnnotator<TemplateFieldDefinition> {

	/** The Constant TEMPLATE_TOKEN_REGEX. */
	private static final String TEMPLATE_TOKEN_REGEX = "<<field:([A-Za-z0-9]+)>>";

	/** The Constant TEMPLATE_TOKEN_PATTERN. */
	private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile(TEMPLATE_TOKEN_REGEX);

	/**
	 * Instantiates a new template field definition annotator which will
	 * assigning confidence 1.0 to all matched field definitions.
	 */
	public TemplateFieldDefinitionAnnotator() {
		super(TEMPLATE_TOKEN_PATTERN, 1.0);
	}

	@Override
	protected TemplateFieldDefinition create(JCas jCas, Matcher matcher) {
		TemplateFieldDefinition field = new TemplateFieldDefinition(jCas);
		field.setName(matcher.group(1));
		return field;
	}

}
