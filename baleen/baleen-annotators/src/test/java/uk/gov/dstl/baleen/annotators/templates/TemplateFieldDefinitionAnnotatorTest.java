package uk.gov.dstl.baleen.annotators.templates;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

public class TemplateFieldDefinitionAnnotatorTest extends AbstractAnnotatorTest {

	private static final String FIELD_TEXT = "Full Name <<field:PersonFullName>>  \n";
	private static final String FIELD2_TEXT = FIELD_TEXT + " Description: \n" + " <<field:Description>>   More text\n";
	private static final String FIELD_REGEX_TEXT = "Email address: \n"
			+ " <<field:email regex=\"\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b\">>   More text\n";
	private static final String FIELD_HTML_REGEX = "HTML: <<field:html regex=\"/^&lt;([a-z]+)([^&lt;]+)*(?:&gt;(.*)&lt;\\/\\1&gt;|\\s+\\/&gt;)$/\">>   More text >>\n";
	private static final String FIELD_NEIGHBOURS = "<<field:one>><<field:two>>";

	public TemplateFieldDefinitionAnnotatorTest() {
		super(TemplateFieldDefinitionAnnotator.class);
	}

	@Test
	public void annotateField() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD_TEXT);
		processJCas();
		TemplateFieldDefinition field = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(10, field.getBegin());
		assertEquals(34, field.getEnd());
		assertEquals("PersonFullName", field.getName());
		assertEquals("<<field:PersonFullName>>", field.getCoveredText());
	}

	@Test
	public void annotate2Fields() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD2_TEXT);
		processJCas();
		TemplateFieldDefinition field1 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(10, field1.getBegin());
		assertEquals(34, field1.getEnd());
		assertEquals("PersonFullName", field1.getName());
		assertEquals("<<field:PersonFullName>>", field1.getCoveredText());

		TemplateFieldDefinition field2 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 1);
		assertEquals(53, field2.getBegin());
		assertEquals(74, field2.getEnd());
		assertEquals("Description", field2.getName());
		assertEquals("<<field:Description>>", field2.getCoveredText());
	}

	@Test
	public void annotateFieldNeighbours() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD_NEIGHBOURS);
		processJCas();
		TemplateFieldDefinition field1 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(0, field1.getBegin());
		assertEquals(13, field1.getEnd());
		assertEquals("one", field1.getName());
		assertEquals("<<field:one>>", field1.getCoveredText());

		TemplateFieldDefinition field2 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 1);
		assertEquals(13, field2.getBegin());
		assertEquals(26, field2.getEnd());
		assertEquals("two", field2.getName());
		assertEquals("<<field:two>>", field2.getCoveredText());
	}

	@Test
	public void annotateFieldWithRegex() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD_REGEX_TEXT);
		processJCas();
		TemplateFieldDefinition field = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(17, field.getBegin());
		assertEquals(82, field.getEnd());
		assertEquals("<<field:email regex=\"\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b\">>", field.getCoveredText());
		assertEquals("email", field.getName());
		assertEquals("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b", field.getRegex());
	}

	@Test
	public void annotateFieldWithHtmlRegex() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD_HTML_REGEX);
		processJCas();
		TemplateFieldDefinition field = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(6, field.getBegin());
		assertEquals(90, field.getEnd());
		assertEquals("<<field:html regex=\"/^&lt;([a-z]+)([^&lt;]+)*(?:&gt;(.*)&lt;\\/\\1&gt;|\\s+\\/&gt;)$/\">>",
				field.getCoveredText());
		assertEquals("html", field.getName());
		assertEquals("/^<([a-z]+)([^<]+)*(?:>(.*)<\\/\\1>|\\s+\\/>)$/", field.getRegex());
	}
}
