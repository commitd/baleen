package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileRecordConsumerTest extends AbstractRecordConsumerTest {

	private static final ObjectMapper YAMLMAPPER = new ObjectMapper(new YAMLFactory());

	private static final ObjectMapper JSONMAPPER = new ObjectMapper(new JsonFactory());

	private Path tempDirectory;

	public FileRecordConsumerTest() {
		super(FileRecordConsumer.class);
	}

	@Before
	public void beforeFileRecordConsumerTest() throws IOException {
		tempDirectory = Files.createTempDirectory(sourceName);
		tempDirectory.toFile().deleteOnExit();
	}

	@Test
	public void testWriteRecordsYaml() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(FileRecordConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());

		Path yamlFile = Paths.get(tempDirectory.toString(), annotatorClass.getSimpleName() + ".yaml");
		yamlFile.toFile().deleteOnExit();

		Map<String, Collection<ExtractedRecord>> records = YAMLMAPPER.readValue(yamlFile.toFile(),
				new TypeReference<Map<String, List<ExtractedRecord>>>() {
				});

		checkRecords(records);

		Files.delete(yamlFile);
	}

	@Test
	public void testWriteRecordsJson() throws AnalysisEngineProcessException, ResourceInitializationException,
			JsonParseException, JsonMappingException, IOException {
		processJCas(FileRecordConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString(),
				FileRecordConsumer.PARAM_OUTPUT_FORMAT, "json");

		Path jsonFile = Paths.get(tempDirectory.toString(), annotatorClass.getSimpleName() + ".json");
		jsonFile.toFile().deleteOnExit();

		Map<String, Collection<ExtractedRecord>> records = JSONMAPPER.readValue(jsonFile.toFile(),
				new TypeReference<Map<String, List<ExtractedRecord>>>() {
				});

		checkRecords(records);

		Files.delete(jsonFile);
	}

	@After
	public void afterFileRecordConsumerTest() throws IOException {
		Files.delete(tempDirectory);
	}

}
