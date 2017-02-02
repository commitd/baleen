package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotates template fields found in documents using a regular expression.
 * <p>
 * Template fields are text surrounded by ASCII double angle bracks, eg <<fieldname>> for the field
 * "fieldname".
 * </p>
 */
public class TemplateFieldDefinitionAnnotator
    extends AbstractRegexAnnotator<TemplateFieldDefinition> {

  public static final String DEFAULT_DATA_TYPE = "String";

  private static final String TEMPLATE_TOKEN_REGEX = "<<([A-Za-z0-9]+)>>";

  private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile(TEMPLATE_TOKEN_REGEX);

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
