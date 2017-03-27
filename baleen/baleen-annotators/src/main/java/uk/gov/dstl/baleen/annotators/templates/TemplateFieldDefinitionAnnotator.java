package uk.gov.dstl.baleen.annotators.templates;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.uima.jcas.JCas;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

/**
 * Annotates template fields found in documents using a regular expression.
 * <p>
 * Template fields are text surrounded by ASCII double angle brackets, eg
 * &lt;&lt;field:fieldname&gt;&gt; for the field "fieldname".
 * </p>
 */
public class TemplateFieldDefinitionAnnotator extends AbstractRegexAnnotator<TemplateFieldDefinition> {

	/** The Constant TEMPLATE_TOKEN_REGEX. */
	private static final String TEMPLATE_TOKEN_REGEX = "<<field:([A-Za-z0-9]+)(\\s.+?)?(?=>>)>>";

	/** The Constant TEMPLATE_TOKEN_PATTERN. */
	private static final Pattern TEMPLATE_TOKEN_PATTERN = Pattern.compile(TEMPLATE_TOKEN_REGEX);

	/** The static DocumentBuilderFactory used to provide DocumentBuilders */
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

	/** The Constant REGEX_ATTRIBUTE */
	private static final String REGEX_ATTRIBUTE = "regex";

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

		if (matcher.group(2) != null) {
			addFieldAttributes(field, matcher.group());
		}
		return field;
	}

	private void addFieldAttributes(TemplateFieldDefinition field, String coveredText) {
		try (InputStream is = IOUtils.toInputStream(coveredText.substring(1, coveredText.length() - 2) + "/>",
				StandardCharsets.UTF_8.name())) {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			NamedNodeMap attributes = doc.getFirstChild().getAttributes();
			Node namedItem = attributes.getNamedItem(REGEX_ATTRIBUTE);
			if (namedItem != null) {
				field.setRegex(namedItem.getTextContent());
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			getMonitor().warn("Failed to read field defintion " + coveredText, e);
		}
	}

}
