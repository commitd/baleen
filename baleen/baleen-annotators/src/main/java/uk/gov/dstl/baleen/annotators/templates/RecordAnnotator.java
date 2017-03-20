package uk.gov.dstl.baleen.annotators.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RecordAnnotator extends BaleenConsumer {

	public static final String PARAM_RECORD_DEFINITIONS_DIRECTORY = "recordDefinitionsDirectory";
	@ConfigurationParameter(name = PARAM_RECORD_DEFINITIONS_DIRECTORY, defaultValue = "recordDefinitions")
	private String recordDefinitionsDirectory = "recordDefinitions";

	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	private final Collection<RecordDefinitionConfiguration> recordDefinitions = new ArrayList<>();

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		readRecordDefinitions();
	}

	private void readRecordDefinitions() throws ResourceInitializationException {
		final Path path = Paths.get(recordDefinitionsDirectory);
		try {
			Files.list(path).filter(Files::isRegularFile).forEach(this::readRecordDefinitionsFromPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(
					new FileNotFoundException("recordDefinitions path not found: " + path.toAbsolutePath()));
		}
	}

	private void readRecordDefinitionsFromPath(final Path path) {
		try {
			List<RecordDefinitionConfiguration> fileDefinitions = objectMapper
					.readValue(Files.newBufferedReader(path, StandardCharsets.UTF_8), objectMapper.getTypeFactory()
							.constructCollectionType(List.class, RecordDefinitionConfiguration.class));
			recordDefinitions.addAll(fileDefinitions);
		} catch (IOException e) {
			getMonitor().warn("Failed to read from recordDefinitions from file " + path, e);
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		for (RecordDefinitionConfiguration recordDefinition : recordDefinitions) {

			Structure preceding = null;
			try {
				String preceedingPath = recordDefinition.getPrecedingPath();
				List<? extends Structure> precedingStructure = SelectorUtils.select(jCas, preceedingPath,
						DEFAULT_STRUCTURAL_PACKAGE);
				if (precedingStructure.size() == 1) {
					preceding = precedingStructure.iterator().next();
				}
			} catch (InvalidParameterException e) {
				getMonitor().warn("Failed to select structure preceeding record " + recordDefinition.getName(), e);
				continue;
			}

			Structure following = null;
			try {
				String followingPath = recordDefinition.getFollowingPath();
				List<? extends Structure> followingStructure = SelectorUtils.select(jCas, followingPath,
						DEFAULT_STRUCTURAL_PACKAGE);
				if (followingStructure.size() == 1) {
					following = followingStructure.iterator().next();
				}
			} catch (InvalidParameterException e) {
				getMonitor().warn("Failed to select structure preceeding record " + recordDefinition.getName(), e);
				continue;
			}

			if (preceding == null || following == null) {
				continue;
			}

			createRecord(jCas, recordDefinition.getName(), preceding.getEnd(), following.getBegin());

			createTemplateFields(jCas, recordDefinition.getFieldPaths());
		}
	}

	private void createTemplateFields(JCas jCas, Map<String, String> fieldPaths) {
		for (Entry<String, String> entry : fieldPaths.entrySet()) {
			String path = entry.getValue();
			String fieldName = entry.getKey();
			try {
				List<? extends Structure> pathStructures = SelectorUtils.select(jCas, path, DEFAULT_STRUCTURAL_PACKAGE);
				if (pathStructures.size() == 1) {
					Structure structure = pathStructures.get(0);
					createField(jCas, fieldName, structure.getBegin(), structure.getEnd());
				} else {
					getMonitor().warn("Expected single structure element for field {} but got {} - ignoring", fieldName,
							pathStructures.size());
				}
			} catch (InvalidParameterException e) {
				getMonitor().warn("Failed to match structure for field " + fieldName, e);
			}
		}
	}

	private void createField(JCas jCas, String name, int begin, int end) {
		TemplateField field = new TemplateField(jCas);
		field.setBegin(begin);
		field.setEnd(end);
		field.setName(name);
		addToJCasIndex(field);

	}

	private void createRecord(JCas jCas, String name, int begin, int end) {
		Record record = new Record(jCas);
		record.setBegin(begin);
		record.setEnd(end);
		record.setName(name);
		addToJCasIndex(record);
	}
}