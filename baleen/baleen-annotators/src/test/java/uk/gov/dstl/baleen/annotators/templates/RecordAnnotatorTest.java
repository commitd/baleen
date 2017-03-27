package uk.gov.dstl.baleen.annotators.templates;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

public class RecordAnnotatorTest extends AbstractAnnotatorTest {
	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String PARA3 = "The quick brown rat jumped over the lazy dog's back.";

	private static final String PARA4 = "The quick brown ant jumped over the lazy dog's back.";

	private static final String PARA5 = "The quick brown elk jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2, PARA3, PARA4, PARA5);

	private static final ObjectMapper YAMLMAPPER = new ObjectMapper(new YAMLFactory());

	private Path tempDirectory;

	public RecordAnnotatorTest() {
		super(RecordAnnotator.class);
	}

	@Before
	public void setup() throws IOException {
		tempDirectory = Files.createTempDirectory(RecordAnnotatorTest.class.getSimpleName());
		jCas.setDocumentText(TEXT);

		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(52);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(53);
		paragraph2.setDepth(1);
		paragraph2.setEnd(105);
		paragraph2.addToIndexes();

		Paragraph paragraph3 = new Paragraph(jCas);
		paragraph3.setBegin(106);
		paragraph3.setDepth(1);
		paragraph3.setEnd(158);
		paragraph3.addToIndexes();

		Paragraph paragraph4 = new Paragraph(jCas);
		paragraph4.setBegin(159);
		paragraph4.setDepth(1);
		paragraph4.setEnd(211);
		paragraph4.addToIndexes();

		Paragraph paragraph5 = new Paragraph(jCas);
		paragraph5.setBegin(212);
		paragraph5.setDepth(1);
		paragraph5.setEnd(264);
		paragraph5.addToIndexes();
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFile()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinition();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			Record record = JCasUtil.selectByIndex(jCas, Record.class, 0);
			assertEquals(52, record.getBegin());
			assertEquals(212, record.getEnd());
			assertEquals(String.join("\n", "", PARA2, PARA3, PARA4, ""), record.getCoveredText());

			TemplateField field1 = JCasUtil.selectByIndex(jCas, TemplateField.class, 0);
			assertEquals(53, field1.getBegin());
			assertEquals(105, field1.getEnd());
			assertEquals(PARA2, field1.getCoveredText());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testMultipleElementsSelectedForField()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createBadRecordDefinition();

		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			Record record = JCasUtil.selectByIndex(jCas, Record.class, 0);
			assertEquals(52, record.getBegin());
			assertEquals(212, record.getEnd());
			assertEquals(String.join("\n", "", PARA2, PARA3, PARA4, ""), record.getCoveredText());

			TemplateField field1 = JCasUtil.selectByIndex(jCas, TemplateField.class, 0);
			assertNull(field1);
		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testNoFieldsInRecord()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createNoFieldsRecordDefinition();

		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			Record record = JCasUtil.selectByIndex(jCas, Record.class, 0);
			assertEquals(158, record.getBegin());
			assertEquals(212, record.getEnd());
			assertEquals(String.join("\n", "", PARA4, ""), record.getCoveredText());

			TemplateField field1 = JCasUtil.selectByIndex(jCas, TemplateField.class, 0);
			assertEquals(53, field1.getBegin());
			assertEquals(105, field1.getEnd());
			assertEquals(PARA2, field1.getCoveredText());

			List<TemplateField> selectCovered = JCasUtil.selectCovered(jCas, TemplateField.class, record);
			assertTrue(selectCovered.isEmpty());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@After
	public void tearDown() throws IOException {
		Files.delete(tempDirectory);
	}

	private Path createGoodRecordDefinition() throws IOException {
		Path definitionFile = Files.createTempFile(tempDirectory, RecordAnnotatorTest.class.getSimpleName(), ".yml");
		String precedingPath = "Paragraph:nth-of-type(1)";
		String followingPath = "Paragraph:nth-of-type(5)";
		List<FieldDefinitionConfiguration> fields = new ArrayList<>();
		fields.add(new FieldDefinitionConfiguration("field", "Paragraph:nth-of-type(2)"));
		RecordDefinitionConfiguration recordDefinition = new RecordDefinitionConfiguration("Test", precedingPath,
				followingPath, fields);
		YAMLMAPPER.writeValue(definitionFile.toFile(), singleton(recordDefinition));
		return definitionFile;
	}

	private Path createBadRecordDefinition() throws IOException {
		Path definitionFile = Files.createTempFile(tempDirectory, RecordAnnotatorTest.class.getSimpleName(), ".yml");
		String precedingPath = "Paragraph:nth-of-type(1)";
		String followingPath = "Paragraph:nth-of-type(5)";
		List<FieldDefinitionConfiguration> fields = new ArrayList<>();
		fields.add(new FieldDefinitionConfiguration("field", "Paragraph"));
		RecordDefinitionConfiguration recordDefinition = new RecordDefinitionConfiguration("Test", precedingPath,
				followingPath, fields);
		YAMLMAPPER.writeValue(definitionFile.toFile(), singleton(recordDefinition));
		return definitionFile;
	}

	private Path createNoFieldsRecordDefinition() throws IOException {
		Path definitionFile = Files.createTempFile(tempDirectory, RecordAnnotatorTest.class.getSimpleName(), ".yml");
		String precedingPath = "Paragraph:nth-of-type(3)";
		String followingPath = "Paragraph:nth-of-type(5)";
		List<FieldDefinitionConfiguration> fields = new ArrayList<>();
		fields.add(new FieldDefinitionConfiguration("field", "Paragraph:nth-of-type(2)"));
		RecordDefinitionConfiguration recordDefinition = new RecordDefinitionConfiguration("Test", precedingPath,
				followingPath, fields);
		YAMLMAPPER.writeValue(definitionFile.toFile(), singleton(recordDefinition));
		return definitionFile;
	}
}
