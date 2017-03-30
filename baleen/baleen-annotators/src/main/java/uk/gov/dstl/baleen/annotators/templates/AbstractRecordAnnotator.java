package uk.gov.dstl.baleen.annotators.templates;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Base class for Annotators that deal with record definitions
 */
public abstract class AbstractRecordAnnotator extends BaleenAnnotator {

	public static final String PARAM_RECORD_DEFINITIONS_DIRECTORY = "recordDefinitionsDirectory";

	/** The record definitions directory. */
	@ConfigurationParameter(name = PARAM_RECORD_DEFINITIONS_DIRECTORY, defaultValue = "recordDefinitions")
	private String recordDefinitionsDirectory = "recordDefinitions";

	protected static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	/** The object mapper, used to read YAML configurations */
	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	/** The record definitions. */
	protected final Multimap<String, RecordDefinitionConfiguration> recordDefinitions = HashMultimap.create();

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		readRecordDefinitions();
	}

	/**
	 * Read all record definitions from YAML configuration files in directory.
	 *
	 * @throws ResourceInitializationException
	 *             if the record definitions path is not found
	 */
	private void readRecordDefinitions() throws ResourceInitializationException {
		final Path path = Paths.get(recordDefinitionsDirectory);
		try {
			Files.list(path).filter(Files::isRegularFile).forEach(this::readRecordDefinitionsFromFile);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	/**
	 * Read record definitions from YAML file.
	 *
	 * @param path
	 *            the path
	 */
	private void readRecordDefinitionsFromFile(final Path path) {
		try {
			List<RecordDefinitionConfiguration> fileDefinitions = objectMapper
					.readValue(Files.newBufferedReader(path, StandardCharsets.UTF_8), objectMapper.getTypeFactory()
							.constructCollectionType(List.class, RecordDefinitionConfiguration.class));
			String namespace = path.toFile().getName();

			recordDefinitions.putAll(namespace, fileDefinitions);
		} catch (IOException e) {
			getMonitor().warn("Failed to read from recordDefinitions from file " + path, e);
		}
	}

	/**
	 * Creates the field annotation.
	 *
	 * @param jCas
	 *            the JCas
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @param value
	 *            the value
	 */
	protected void createFieldAnnotation(JCas jCas, String source, String name, int begin, int end, String value) {
		TemplateField field = new TemplateField(jCas);
		field.setBegin(begin);
		field.setEnd(end);
		field.setName(name);
		field.setSource(source);
		field.setValue(value);
		addToJCasIndex(field);
	}

	/**
	 * Creates the record annotation.
	 *
	 * @param jCas
	 *            the JCas
	 * @param source
	 *            the source
	 * @param name
	 *            the name
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 */
	protected void createRecordAnnotation(JCas jCas, String source, String name, int begin, int end) {
		Record record = new Record(jCas);
		record.setBegin(begin);
		record.setSource(source);
		record.setEnd(end);
		record.setName(name);
		addToJCasIndex(record);
	}
}