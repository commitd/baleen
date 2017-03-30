package uk.gov.dstl.baleen.annotators.templates;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class FieldJoiningAnnotatorTest extends AbstractAnnotatorTest {

	private static final String TEXT = "The quick brown fox jumped over the lazy dog's back.";

	public FieldJoiningAnnotatorTest() {
		super(FieldJoiningAnnotator.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);

		Record record = new Record(jCas);
		record.setName("report");
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
		field2.setName("spectator");
		field2.addToIndexes();
	}

	@Test
	public void testAthleteIsMadePerson() throws AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(FieldJoiningAnnotator.PARAM_RECORD, "report", FieldJoiningAnnotator.PARAM_FIELD_NAME, "fullName",
				FieldJoiningAnnotator.PARAM_TEMPLATE, "{{athlete}}, {{spectator}}");
		Collection<TemplateField> fields = JCasUtil.select(jCas, TemplateField.class);
		assertEquals(3, fields.size());

		List<TemplateField> fullNameFields = fields.stream().filter(f -> extracted(f))
				.collect(Collectors.toList());
		assertEquals(1, fullNameFields.size());

		TemplateField fullName = fullNameFields.iterator().next();
		assertEquals("fox, dog", fullName.getValue());
		assertEquals(16, fullName.getBegin());
		assertEquals(44, fullName.getEnd());
	}

	private boolean extracted(TemplateField f) {
		return f.getName().equals("fullName");
	}
}
