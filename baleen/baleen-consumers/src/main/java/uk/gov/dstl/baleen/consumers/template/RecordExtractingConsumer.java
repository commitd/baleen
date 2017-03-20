package uk.gov.dstl.baleen.consumers.template;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

public class RecordExtractingConsumer extends BaleenConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordExtractingConsumer.class);

	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	public static final String PARAM_OUTPUT_FILE = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, defaultValue = "records")
	private String outputDirectory = "records";

	public static final String PARAM_RECORD_DEFINITIONS_DIRECTORY = "recordDefinitionsDirectory";
	@ConfigurationParameter(name = PARAM_RECORD_DEFINITIONS_DIRECTORY, defaultValue = "recordDefinitions")
	private String recordDefinitionsDirectory = "recordDefinitions";

	public static final String PARAM_OUTPUT_FORMAT = "outputFormat";
	@ConfigurationParameter(name = PARAM_OUTPUT_FORMAT, defaultValue = "yaml")
	private String outputFormat = "yaml";

	private Set<RecordDefinition> recordDefinitions = new HashSet<>();

	private ObjectMapper mapper;

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		if ("json".equals(outputFormat)) {
			mapper = new ObjectMapper();
		} else {
			mapper = new ObjectMapper(new YAMLFactory());

		}
		readRecordDefinitions();
	}

	private void readRecordDefinitions() throws ResourceInitializationException {
		final Path path = Paths.get(recordDefinitionsDirectory);
		try {
			Files.list(path).filter(Files::isRegularFile).forEach(this::readRecordDefinitionsFromPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(
					new FileNotFoundException("Template selector path not found: " + path.toAbsolutePath()));
		}
	}

	private void readRecordDefinitionsFromPath(final Path path) {
		try {
			List<RecordDefinition> fileDefinitions = mapper.readValue(
					Files.newBufferedReader(path, StandardCharsets.UTF_8),
					mapper.getTypeFactory().constructCollectionType(List.class, RecordDefinition.class));
			recordDefinitions.addAll(fileDefinitions);
		} catch (IOException e) {
			LOGGER.warn("Failed to read from selectors file " + path, e);
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<ExtractedRecord> records = new ArrayList<>();
		for (RecordDefinition recordDefinition : recordDefinitions) {
			String recordPath = recordDefinition.getRecordBeginPath();

			Map<String, String> fields = new HashMap<>();
			for (Entry<String, String> field : recordDefinition.getFieldPaths().entrySet()) {
				String name = field.getKey();
				String path = field.getValue();
				try {
					List<? extends Structure> elements = SelectorUtils.select(jCas, path, DEFAULT_STRUCTURAL_PACKAGE);
					if (elements.size() > 1) {
						LOGGER.warn(
								"Record definition {} - found more than one element with selector for {} - discarding all but first match",
								recordDefinition.getName(), name);
					} else if (elements.isEmpty()) {
						LOGGER.info("Found no match for record definition {} selector {} '{}'",
								recordDefinition.getName(), name, path);
						continue;
					}
					Structure element = elements.get(0);
					fields.put(name, element.getCoveredText());
				} catch (InvalidParameterException e) {
					LOGGER.warn("Failed to select annotations for field " + name + " in record definition "
							+ recordDefinition.getName(), e);
				}
			}
			records.add(new ExtractedRecord(recordDefinition.getName(), fields));

		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());
		try (Writer w = createOutputWriter(documentSourceName)) {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			mapper.writeValue(w, records);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private Writer createOutputWriter(final String documentSourceName) throws IOException {
		Path directoryPath = Paths.get(outputDirectory);
		if (!Files.exists(directoryPath)) {
			Files.createDirectories(directoryPath);
		}
		String baseName = FilenameUtils.getBaseName(documentSourceName);
		Path outputFilePath = directoryPath.resolve(baseName + ".yml");

		if (Files.exists(outputFilePath)) {
			LOGGER.warn("Overwriting existing output properties file {}", outputFilePath.toString());
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

}