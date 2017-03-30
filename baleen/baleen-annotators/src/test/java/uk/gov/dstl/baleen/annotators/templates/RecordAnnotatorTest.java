package uk.gov.dstl.baleen.annotators.templates;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

public class RecordAnnotatorTest extends AbstractRecordAnnotatorTest {

	public RecordAnnotatorTest() {
		super(RecordAnnotator.class);
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFile()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinition();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			assertRecordCoversParas2to4();

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(53, field1.getBegin());
			assertEquals(105, field1.getEnd());
			assertEquals(PARA2, field1.getCoveredText());
			assertEquals(PARA2, field1.getValue());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFileWithRegex()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinitionWithRegex();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			assertRecordCoversParas2to4();

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(69, field1.getBegin());
			assertEquals(72, field1.getEnd());
			assertEquals("cat", field1.getCoveredText());
			assertEquals("cat", field1.getValue());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFileWithRegexRequired()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinitionWithRegexRequired();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			assertRecordCoversParas2to4();

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(122, field1.getBegin());
			assertEquals(125, field1.getEnd());
			assertEquals("rat", field1.getCoveredText());
			assertEquals("rat", field1.getValue());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFileWithRegexDefaultNotNeeded()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinitionWithRegexDefaultNotNeeded();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			assertRecordCoversParas2to4();

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(179, field1.getBegin());
			assertEquals(185, field1.getEnd());
			assertEquals("jumped", field1.getCoveredText());
			assertEquals("jumped", field1.getValue());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFileWithRegexDefaultUsed()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinitionWithRegexDefaultNeeded();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			assertRecordCoversParas2to4();

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(159, field1.getBegin());
			assertEquals(159, field1.getEnd());
			assertEquals("", field1.getCoveredText());
			assertEquals("horse", field1.getValue());

		} finally {
			Files.delete(definitionFile);
		}
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFileWithRegexMissingRequired()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createGoodRecordDefinitionWithRegexRequiredAndMissing();
		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());
			assertFalse(JCasUtil.exists(jCas, TemplateField.class));
			assertTrue(JCasUtil.exists(jCas, Record.class));
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
			assertRecordCoversParas2to4();
			assertFalse(JCasUtil.exists(jCas, TemplateField.class));
		} finally {
			Files.delete(definitionFile);
		}
	}

	protected Record assertRecordCoversParas2to4() {
		Record record = JCasUtil.selectSingle(jCas, Record.class);
		assertEquals(52, record.getBegin());
		assertEquals(212, record.getEnd());
		assertEquals(String.join("\n", "", PARA2, PARA3, PARA4, ""), record.getCoveredText());
		return record;
	}

	@Test
	public void testNoFieldsInRecord()
			throws AnalysisEngineProcessException, ResourceInitializationException, IOException {

		Path definitionFile = createNoFieldsRecordDefinition();

		try {
			processJCas(RecordAnnotator.PARAM_RECORD_DEFINITIONS_DIRECTORY, tempDirectory.toString());

			Record record = JCasUtil.selectSingle(jCas, Record.class);
			assertEquals(158, record.getBegin());
			assertEquals(212, record.getEnd());
			assertEquals(String.join("\n", "", PARA4, ""), record.getCoveredText());

			TemplateField field1 = JCasUtil.selectSingle(jCas, TemplateField.class);
			assertEquals(53, field1.getBegin());
			assertEquals(105, field1.getEnd());
			assertEquals(PARA2, field1.getCoveredText());
			assertEquals(PARA2, field1.getValue());

			assertFalse(JCasUtil.contains(jCas, record, TemplateField.class));

		} finally {
			Files.delete(definitionFile);
		}
	}

	private Path createGoodRecordDefinition() throws IOException {
		return createRecord("test", new FieldDefinitionConfiguration("field", "Paragraph:nth-of-type(2)"));
	}

	private Path createGoodRecordDefinitionWithRegex() throws IOException {
		FieldDefinitionConfiguration fieldDefinitionConfiguration = new FieldDefinitionConfiguration("field",
				"Paragraph:nth-of-type(2)");
		fieldDefinitionConfiguration.setRegex("(?<=brown )(.*)(?= jumped)");
		return createRecord("test", fieldDefinitionConfiguration);
	}

	private Path createGoodRecordDefinitionWithRegexRequired() throws IOException {
		FieldDefinitionConfiguration fieldDefinitionConfiguration = new FieldDefinitionConfiguration("field",
				"Paragraph:nth-of-type(3)");
		fieldDefinitionConfiguration.setRegex("(?<=brown )(.*)(?= jumped)");
		fieldDefinitionConfiguration.setRequired(true);
		return createRecord("test", fieldDefinitionConfiguration);
	}

	private Path createGoodRecordDefinitionWithRegexRequiredAndMissing() throws IOException {
		FieldDefinitionConfiguration fieldDefinitionConfiguration = new FieldDefinitionConfiguration("field",
				"Paragraph:nth-of-type(3)");
		fieldDefinitionConfiguration.setRegex("(?<=white )(.*)(?= jumped)");
		fieldDefinitionConfiguration.setRequired(true);
		return createRecord("test", fieldDefinitionConfiguration);
	}

	private Path createGoodRecordDefinitionWithRegexDefaultNotNeeded() throws IOException {
		FieldDefinitionConfiguration fieldDefinitionConfiguration = new FieldDefinitionConfiguration("field",
				"Paragraph:nth-of-type(4)");
		fieldDefinitionConfiguration.setRegex("(?<=ant )(.*)(?= over)");
		fieldDefinitionConfiguration.setDefaultValue("crawled");
		return createRecord("test", fieldDefinitionConfiguration);
	}

	private Path createGoodRecordDefinitionWithRegexDefaultNeeded() throws IOException {
		FieldDefinitionConfiguration fieldDefinitionConfiguration = new FieldDefinitionConfiguration("field",
				"Paragraph:nth-of-type(4)");
		fieldDefinitionConfiguration.setRegex("(?<=white )(.*)(?= jumped)");
		fieldDefinitionConfiguration.setDefaultValue("horse");
		return createRecord("test", fieldDefinitionConfiguration);
	}

	private Path createBadRecordDefinition() throws IOException {
		return createRecord("test", new FieldDefinitionConfiguration("field", "Paragraph"));
	}

	private Path createNoFieldsRecordDefinition() throws IOException {
		List<FieldDefinitionConfiguration> fields = new ArrayList<>();
		fields.add(new FieldDefinitionConfiguration("field", "Paragraph:nth-of-type(2)"));
		Path definitionFile = Files.createTempFile(tempDirectory, AbstractRecordAnnotatorTest.class.getSimpleName(),
				".yml");
		String precedingPath = "Paragraph:nth-of-type(3)";
		String followingPath = "Paragraph:nth-of-type(5)";
		RecordDefinitionConfiguration recordDefinition = new RecordDefinitionConfiguration("test", precedingPath,
				followingPath, fields);
		YAMLMAPPER.writeValue(definitionFile.toFile(), singleton(recordDefinition));
		return definitionFile;
	}

}
