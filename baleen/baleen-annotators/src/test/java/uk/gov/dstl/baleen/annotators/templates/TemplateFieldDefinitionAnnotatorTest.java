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

	public TemplateFieldDefinitionAnnotatorTest() {
		super(TemplateFieldDefinitionAnnotator.class);
	}

	@Test
	public void annotateField() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD_TEXT);
		processJCas();
		TemplateFieldDefinition record = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(10, record.getBegin());
		assertEquals(34, record.getEnd());
		assertEquals("<<field:PersonFullName>>", record.getCoveredText());
	}

	@Test
	public void annotate2Fields() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(FIELD2_TEXT);
		processJCas();
		TemplateFieldDefinition field1 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 0);
		assertEquals(10, field1.getBegin());
		assertEquals(34, field1.getEnd());
		assertEquals("<<field:PersonFullName>>", field1.getCoveredText());

		TemplateFieldDefinition field2 = JCasUtil.selectByIndex(jCas, TemplateFieldDefinition.class, 1);
		assertEquals(53, field2.getBegin());
		assertEquals(74, field2.getEnd());
		assertEquals("<<field:Description>>", field2.getCoveredText());
	}

}
