package uk.gov.dstl.baleen.annotators.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

import java.io.IOException;

public class FieldToEntityAnnnotatorTest extends AbstractAnnotatorTest {

	private static final String TEXT = "The quick brown fox jumped over the lazy dog's back.";

	public FieldToEntityAnnnotatorTest() {
		super(FieldToEntityAnnotator.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);

		Record record = new Record(jCas);
		record.setName("report");
		record.setSource("brownSauce");
		record.setBegin(0);
		record.setEnd(52);
		record.addToIndexes();

		TemplateField field1 = new TemplateField(jCas);
		field1.setBegin(16);
		field1.setEnd(19);
		field1.setName("athlete");
		field1.addToIndexes();

		TemplateField field2 = new TemplateField(jCas);
		field2.setBegin(41);
		field2.setEnd(44);
		field2.setName("spectactor");
		field2.addToIndexes();
	}

	@Test
	public void testAthleteIsMadePersonNoSource()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(FieldToEntityAnnotator.PARAM_ENTITY_TYPE, "common.Person", FieldToEntityAnnotator.PARAM_FIELD_NAME,
				"athlete", FieldToEntityAnnotator.PARAM_RECORD_NAME, "report");
		Person person = JCasUtil.selectSingle(jCas, Person.class);
		assertEquals("fox", person.getValue());
		assertEquals(16, person.getBegin());
		assertEquals(19, person.getEnd());
	}

	@Test
	public void testAthleteIsMadePersonSource() throws AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(FieldToEntityAnnotator.PARAM_ENTITY_TYPE, "common.Person", FieldToEntityAnnotator.PARAM_FIELD_NAME,
				"athlete", FieldToEntityAnnotator.PARAM_RECORD_NAME, "report", FieldToEntityAnnotator.PARAM_SOURCE,
				"brownSauce");
		Person person = JCasUtil.selectSingle(jCas, Person.class);
		assertEquals("fox", person.getValue());
		assertEquals(16, person.getBegin());
		assertEquals(19, person.getEnd());
	}

	@Test
	public void testAthleteIsMadePersonOtherSource()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(FieldToEntityAnnotator.PARAM_ENTITY_TYPE, "common.Person", FieldToEntityAnnotator.PARAM_FIELD_NAME,
				"athlete", FieldToEntityAnnotator.PARAM_RECORD_NAME, "report", FieldToEntityAnnotator.PARAM_SOURCE,
				"ketchup");
		assertFalse(JCasUtil.iterator(jCas, Person.class).hasNext());
	}

}
