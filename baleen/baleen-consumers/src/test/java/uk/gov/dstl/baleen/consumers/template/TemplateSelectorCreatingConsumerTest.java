package uk.gov.dstl.baleen.consumers.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

public class TemplateSelectorCreatingConsumerTest extends AbstractAnnotatorTest {

	private static final String EXPECTED_OUTPUT_FILENAME = TemplateSelectorCreatingConsumerTest.class.getSimpleName()
			+ ".properties";

	private static final String SOURCEURI = TemplateSelectorCreatingConsumerTest.class.getSimpleName() + ".txt";

	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String PARA3 = "The quick brown rat jumped over the lazy dog's back.";

	private static final String PARA4 = "The quick brown bat jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2, PARA3, PARA4);

	private Path tempDirectory;

	public TemplateSelectorCreatingConsumerTest() {
		super(TemplateSelectorCreatingConsumer.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);
		tempDirectory = Files.createTempDirectory(TemplateSelectorCreatingConsumerTest.class.getSimpleName());
		tempDirectory.toFile().deleteOnExit();

		DocumentAnnotation documentAnnotation = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		documentAnnotation.setSourceUri(SOURCEURI);

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
		paragraph4.setEnd(212);
		paragraph4.addToIndexes();

		TemplateFieldDefinition field1 = new TemplateFieldDefinition(jCas);
		field1.setBegin(72);
		field1.setEnd(75);
		field1.setName("field1");
		field1.addToIndexes();

		TemplateFieldDefinition field2 = new TemplateFieldDefinition(jCas);
		field2.setBegin(123);
		field2.setEnd(140);
		field2.setName("field2");
		field2.addToIndexes();

	}

	@Test
	public void testSelectors() throws IOException, AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(TemplateSelectorCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		checkSelectors();
	}

	@Test
	public void testSelectorsFileAlreadyExists()
			throws IOException, AnalysisEngineProcessException, ResourceInitializationException {
		assertTrue(Paths.get(tempDirectory.toString(), EXPECTED_OUTPUT_FILENAME).toFile().createNewFile());
		processJCas(TemplateSelectorCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		checkSelectors();
	}

	@Test
	public void testSelectorsCustomTypeList()
			throws IOException, AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(TemplateSelectorCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				TemplateSelectorCreatingConsumer.PARAM_TYPE_NAMES, new String[] { "Paragraph" });
		checkSelectors();
	}

	@Test(expected = ResourceInitializationException.class)
	public void testInvalidTypes() throws IOException, AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(TemplateSelectorCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				TemplateSelectorCreatingConsumer.PARAM_TYPE_NAMES, new String[] { "MadeUpClass" });
	}

	@Test
	public void testEmptyTypes() throws IOException, AnalysisEngineProcessException, ResourceInitializationException {
		processJCas(TemplateSelectorCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				TemplateSelectorCreatingConsumer.PARAM_TYPE_NAMES, new String[] {});
		checkSelectors();
	}

	private void checkSelectors() throws IOException {
		Path propertiesFile = Paths.get(tempDirectory.toString(), EXPECTED_OUTPUT_FILENAME);
		propertiesFile.toFile().deleteOnExit();
		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesFile));

		assertEquals("Paragraph:nth-of-type(2)", properties.get("field1"));
		assertEquals("Paragraph:nth-of-type(3)", properties.get("field2"));

		Files.delete(propertiesFile);
	}

	@After
	public void tearDown() throws IOException {
		Files.delete(tempDirectory);
	}

}
