package uk.gov.dstl.baleen.consumers.template;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
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
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.consumers.template.ExtractedRecord.Kind;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

public class RecordConsumerTest extends AbstractAnnotatorTest {

	private static final String SOURCEURI = RecordConsumerTest.class.getSimpleName() + ".txt";

	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String PARA3 = "The quick brown rat jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2, PARA3);

	private static final ObjectMapper YAMLMAPPER = new ObjectMapper(new YAMLFactory());

	private static final ObjectMapper JSONMAPPER = new ObjectMapper(new JsonFactory());

	private Path tempDirectory;

	public RecordConsumerTest() {
		super(RecordConsumer.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);
		tempDirectory = Files.createTempDirectory(RecordConsumerTest.class.getSimpleName());
		tempDirectory.toFile().deleteOnExit();

		DocumentAnnotation documentAnnotation = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		documentAnnotation.setSourceUri(SOURCEURI);

		Record record1 = new Record(jCas);
		record1.setBegin(0);
		record1.setEnd(52);
		record1.setName("record1");
		record1.addToIndexes();

		TemplateField record1Field1 = new TemplateField(jCas);
		record1Field1.setBegin(0);
		record1Field1.setEnd(15);
		record1Field1.setName("record1Field1");
		record1Field1.addToIndexes();

		TemplateField record1Field2 = new TemplateField(jCas);
		record1Field2.setBegin(16);
		record1Field2.setEnd(31);
		record1Field2.setName("record1Field2");
		record1Field2.addToIndexes();

		Record record2 = new Record(jCas);
		record2.setBegin(53);
		record2.setEnd(105);
		record2.setName("record2");
		record2.addToIndexes();

		TemplateField record2Field1 = new TemplateField(jCas);
		record2Field1.setBegin(53);
		record2Field1.setEnd(68);
		record2Field1.setName("record2Field1");
		record2Field1.addToIndexes();

		TemplateField record2Field2 = new TemplateField(jCas);
		record2Field2.setBegin(69);
		record2Field2.setEnd(84);
		record2Field2.setName("record2Field2");
		record2Field2.addToIndexes();

		TemplateField noRecordField1 = new TemplateField(jCas);
		noRecordField1.setBegin(106);
		noRecordField1.setEnd(121);
		noRecordField1.setName("noRecordField1");
		noRecordField1.addToIndexes();

		TemplateField noRecordField2 = new TemplateField(jCas);
		noRecordField2.setBegin(122);
		noRecordField2.setEnd(137);
		noRecordField2.setName("noRecordField2");
		noRecordField2.addToIndexes();
	}

	@Test
	public void testWriteRecordsYaml() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(RecordConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());

		Path yamlFile = Paths.get(tempDirectory.toString(), RecordConsumerTest.class.getSimpleName() + ".yaml");
		yamlFile.toFile().deleteOnExit();

		List<ExtractedRecord> records = YAMLMAPPER.readValue(yamlFile.toFile(),
				YAMLMAPPER.getTypeFactory().constructCollectionType(List.class, ExtractedRecord.class));

		checkRecords(records);

		Files.delete(yamlFile);
	}

	@Test
	public void testWriteRecordsJson() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(RecordConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(), RecordConsumer.PARAM_OUTPUT_FORMAT,
				"json");

		Path jsonFile = Paths.get(tempDirectory.toString(), RecordConsumerTest.class.getSimpleName() + ".json");
		jsonFile.toFile().deleteOnExit();

		List<ExtractedRecord> records = JSONMAPPER.readValue(jsonFile.toFile(),
				JSONMAPPER.getTypeFactory().constructCollectionType(List.class, ExtractedRecord.class));

		checkRecords(records);

		Files.delete(jsonFile);
	}

	private void checkRecords(List<ExtractedRecord> records) {
		ExtractedRecord record1 = records.stream()
				.filter(p -> p.getKind().equals(Kind.NAMED) && p.getName().equals("record1"))
				.collect(Collectors.toList()).get(0);
		assertEquals(Kind.NAMED, record1.getKind());
		assertEquals(2, record1.getFields().size());
		assertEquals("The quick brown", record1.getFields().get("record1Field1"));
		assertEquals("fox jumped over", record1.getFields().get("record1Field2"));

		ExtractedRecord record2 = records.stream()
				.filter(p -> p.getKind().equals(Kind.NAMED) && p.getName().equals("record2"))
				.collect(Collectors.toList()).get(0);
		assertEquals(Kind.NAMED, record1.getKind());
		assertEquals(2, record2.getFields().size());
		assertEquals("The quick brown", record2.getFields().get("record2Field1"));
		assertEquals("cat jumped over", record2.getFields().get("record2Field2"));

		ExtractedRecord defaultRecord = records.stream().filter(p -> p.getKind().equals(Kind.DEFAULT))
				.collect(Collectors.toList()).get(0);
		assertEquals(null, defaultRecord.getName());
		assertEquals(2, defaultRecord.getFields().size());
		assertEquals("The quick brown", defaultRecord.getFields().get("noRecordField1"));
		assertEquals("rat jumped over", defaultRecord.getFields().get("noRecordField2"));
	}

	@After
	public void tearDown() throws IOException {
		Files.delete(tempDirectory);
	}

}
