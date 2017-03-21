package uk.gov.dstl.baleen.consumers.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionConfiguration;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionConfiguration.Kind;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.templates.RecordDefinition;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;

public class RecordDefinitionConfigurationCreatingConsumerTest extends AbstractAnnotatorTest {

	private static final String SOURCEURI = RecordDefinitionConfigurationCreatingConsumerTest.class.getSimpleName()
			+ ".yaml";

	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String PARA3 = "The quick brown rat jumped over the lazy dog's back.";

	private static final String PARA4 = "The quick brown bat jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2, PARA3, PARA4);

	private static final ObjectMapper YAMLMAPPER = new ObjectMapper(new YAMLFactory());

	private Path tempDirectory;

	public RecordDefinitionConfigurationCreatingConsumerTest() {
		super(RecordDefinitionConfigurationCreatingConsumer.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);
		tempDirectory = Files
				.createTempDirectory(RecordDefinitionConfigurationCreatingConsumerTest.class.getSimpleName());
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

		RecordDefinition record1 = new RecordDefinition(jCas);
		record1.setBegin(53);
		record1.setEnd(158);
		record1.setName("record1");
		record1.addToIndexes();

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

		TemplateFieldDefinition field3 = new TemplateFieldDefinition(jCas);
		field3.setBegin(17);
		field3.setEnd(20);
		field3.setName("noRecordField");
		field3.addToIndexes();
	}

	@Test
	public void testRecordDefinition() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(RecordDefinitionConfigurationCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		checkDefinitions();
	}

	@Test
	public void testRecordDefinitionOutputFileAlreadyExists() throws AnalysisEngineProcessException,
			ResourceInitializationException, JsonParseException, JsonMappingException, IOException {
		assertTrue(Paths
				.get(tempDirectory.toString(),
						RecordDefinitionConfigurationCreatingConsumerTest.class.getSimpleName() + ".yaml")
				.toFile().createNewFile());
		processJCas(RecordDefinitionConfigurationCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		checkDefinitions();
	}

	@Test
	public void testRecordDefinitionCustomStructureClassList() throws AnalysisEngineProcessException,
			ResourceInitializationException, JsonParseException, JsonMappingException, IOException {
		processJCas(RecordDefinitionConfigurationCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				RecordDefinitionConfigurationCreatingConsumer.PARAM_TYPE_NAMES, new String[] { "Paragraph" });
		checkDefinitions();
	}

	@Test
	public void testRecordDefinitionEmptyStructureClassList() throws AnalysisEngineProcessException,
			ResourceInitializationException, JsonParseException, JsonMappingException, IOException {
		processJCas(RecordDefinitionConfigurationCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				RecordDefinitionConfigurationCreatingConsumer.PARAM_TYPE_NAMES, new String[] {});
		checkDefinitions();
	}

	@Test(expected = ResourceInitializationException.class)
	public void testInvalidTypes() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(RecordDefinitionConfigurationCreatingConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				RecordDefinitionConfigurationCreatingConsumer.PARAM_TYPE_NAMES, new String[] { "MadeUpClass" });
	}

	private void checkDefinitions() throws IOException, JsonParseException, JsonMappingException {
		Path yamlFile = Paths.get(tempDirectory.toString(),
				RecordDefinitionConfigurationCreatingConsumerTest.class.getSimpleName() + ".yaml");
		yamlFile.toFile().deleteOnExit();

		List<RecordDefinitionConfiguration> definitions = YAMLMAPPER.readValue(yamlFile.toFile(),
				YAMLMAPPER.getTypeFactory().constructCollectionType(List.class, RecordDefinitionConfiguration.class));

		RecordDefinitionConfiguration record1 = definitions.stream()
				.filter(p -> p.getKind().equals(Kind.NAMED) && p.getName().equals("record1"))
				.collect(Collectors.toList()).get(0);
		assertEquals(Kind.NAMED, record1.getKind());
		assertEquals(2, record1.getFieldPaths().size());
		assertEquals("Paragraph:nth-of-type(2)", record1.getFieldPaths().get("field1"));
		assertEquals("Paragraph:nth-of-type(3)", record1.getFieldPaths().get("field2"));
		assertEquals("Paragraph:nth-of-type(1)", record1.getPrecedingPath());
		assertEquals("Paragraph:nth-of-type(4)", record1.getFollowingPath());

		RecordDefinitionConfiguration defaultRecord = definitions.stream().filter(p -> p.getKind().equals(Kind.DEFAULT))
				.collect(Collectors.toList()).get(0);
		assertEquals(null, defaultRecord.getName());
		assertEquals(1, defaultRecord.getFieldPaths().size());
		assertEquals("Paragraph:nth-of-type(1)", defaultRecord.getFieldPaths().get("noRecordField"));

		Files.delete(yamlFile);
	}

	@After
	public void tearDown() throws IOException {
		Files.delete(tempDirectory);
	}
}
