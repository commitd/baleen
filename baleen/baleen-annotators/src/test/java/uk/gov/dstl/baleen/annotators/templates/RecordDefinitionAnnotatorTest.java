package uk.gov.dstl.baleen.annotators.templates;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.templates.RecordDefinition;

public class RecordDefinitionAnnotatorTest extends AbstractAnnotatorTest {

	private static final String RECORD_TEXT = " We may also find text of interest in table form, such as this:<<record:foo:begin>> \n"
			+ "    Full Name:  \n" + "    <<field:PersonFullName>> \n" + " Description: \n"
			+ " <<field:Description>><<record:foo:end>>\n" + "Some text afterwards.\n";

	private static final String RECORD2_TEXT = RECORD_TEXT + RECORD_TEXT;

	public RecordDefinitionAnnotatorTest() {
		super(RecordDefinitionAnnotator.class);
	}

	@Test
	public void annotateRecord() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(RECORD_TEXT);
		processJCas();
		RecordDefinition record = JCasUtil.selectByIndex(jCas, RecordDefinition.class, 0);
		assertEquals(83, record.getBegin());
		assertEquals(169, record.getEnd());
		assertEquals(" \n" + "    Full Name:  \n" + "    <<field:PersonFullName>> \n" + " Description: \n"
				+ " <<field:Description>>", record.getCoveredText());
	}

	@Test
	public void annotate2Record() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText(RECORD2_TEXT);
		processJCas();
		RecordDefinition record = JCasUtil.selectByIndex(jCas, RecordDefinition.class, 0);
		assertEquals(83, record.getBegin());
		assertEquals(169, record.getEnd());
		assertEquals(" \n" + "    Full Name:  \n" + "    <<field:PersonFullName>> \n" + " Description: \n"
				+ " <<field:Description>>", record.getCoveredText());

		RecordDefinition record2 = JCasUtil.selectByIndex(jCas, RecordDefinition.class, 1);
		assertEquals(293, record2.getBegin());
		assertEquals(379, record2.getEnd());
		assertEquals(" \n" + "    Full Name:  \n" + "    <<field:PersonFullName>> \n" + " Description: \n"
				+ " <<field:Description>>", record2.getCoveredText());
	}

}
