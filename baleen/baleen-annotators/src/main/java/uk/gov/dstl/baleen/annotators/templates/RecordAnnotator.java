package uk.gov.dstl.baleen.annotators.templates;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionConfiguration.Kind;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Using previously created record definitions, creates annotations for records
 * and the the fields contained within them.
 * 
 * <p>
 * Each YAML configuration file contains multiple definitions in an array/list,
 * with each definition being an object with following fields:
 * <p>
 * <dl>
 * <dt>fields</dt>
 * <dd>a dictionary / map of name and structural selector path to extract the
 * field from the document. A TemplateField annotation is created for each
 * matched path.</dd>
 * 
 * <dt>kind</dt>
 * <dd>Whether the field selectors above should be used to create a
 * <code>NAMED</code> record, in which case a name field will also be supplied,
 * or these are not part of an explicit record, and thus gathered into a
 * <code>DEFAULT</code> record, so they are still annotated as
 * TemplateFields.</dd>
 * <dt>name</dt>
 * <dd>Only present on <code>NAMED</code> RecordDefinitions, and is populated
 * with the name of the record.
 * <dd>
 * </dl>
 * 
 * An example YAML configuration could be:
 * 
 * <pre>
---
- name: "NamedRecord"
  kind: "NAMED"
  fieldPaths:
    Description: "Paragraph:nth-of-type(8)"
    FullName: "Table:nth-of-type(2) > TableBody > TableRow:nth-of-type(2) >\
      \ TableCell:nth-of-type(2) > Paragraph"
  precedingPath: "Paragraph:nth-of-type(6)"
  followingPath: "Paragraph:nth-of-type(10)"
- kind: "DEFAULT"
  fieldPaths:
    DocumentTitle: "Heading:nth-of-type(2)"
    DocumentDate: "Paragraph:nth-of-type(3)"
 * </pre>
 * <p>
 * Configurations are typically created by running a pipeline with the
 * RecordDefinitionConfigurationCreatingConsumer, which uses annotations created
 * by RecordDefinitionAnnotation and TemplateFieldDefinitionAnnotator running
 * over template documents.
 * </p>
 */
public class RecordAnnotator extends BaleenConsumer {

	public static final String PARAM_RECORD_DEFINITIONS_DIRECTORY = "recordDefinitionsDirectory";

	/** The record definitions directory. */
	@ConfigurationParameter(name = PARAM_RECORD_DEFINITIONS_DIRECTORY, defaultValue = "recordDefinitions")
	private String recordDefinitionsDirectory = "recordDefinitions";

	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	/** The object mapper, used to read YAML configurations */
	private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

	/** The record definitions. */
	private final Multimap<String, RecordDefinitionConfiguration> recordDefinitions = HashMultimap.create();

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
			String namespace = FilenameUtils.getBaseName(path.toString());
			recordDefinitions.putAll(namespace, fileDefinitions);
		} catch (IOException e) {
			getMonitor().warn("Failed to read from recordDefinitions from file " + path, e);
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		for (Entry<String, RecordDefinitionConfiguration> entry : recordDefinitions.entries()) {
			RecordDefinitionConfiguration recordDefinition = entry.getValue();
			String source = entry.getKey();
			createTemplateFields(source, recordDefinition.getFieldPaths(), jCas);
			if (recordDefinition.getKind() == Kind.NAMED) {
				createRecord(source, recordDefinition, jCas);
			}
		}
	}

	/**
	 * Creates the record based on the paths in the record definition.
	 * 
	 * If errors occur during selection these are logged.
	 * 
	 * @param source
	 *
	 * @param recordDefinition
	 *            the record definition
	 * @param jCas
	 *            the jCas
	 */
	private void createRecord(String source, RecordDefinitionConfiguration recordDefinition, JCas jCas) {
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
			return;
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
			return;
		}

		if (preceding == null || following == null) {
			return;
		}

		createRecordAnnotation(jCas, source, recordDefinition.getName(), preceding.getEnd(), following.getBegin());
	}

	/**
	 * Creates the template fields based on the field definition selectors in
	 * the record definition.
	 *
	 * @param jCas
	 *            the jCas
	 * @param fieldPaths
	 *            the field paths
	 */
	private void createTemplateFields(String source, Map<String, String> fieldPaths, JCas jCas) {
		for (Entry<String, String> entry : fieldPaths.entrySet()) {
			String path = entry.getValue();
			String fieldName = entry.getKey();
			try {
				List<? extends Structure> pathStructures = SelectorUtils.select(jCas, path, DEFAULT_STRUCTURAL_PACKAGE);
				if (pathStructures.size() == 1) {
					Structure structure = pathStructures.get(0);
					createFieldAnnotation(jCas, source, fieldName, structure.getBegin(), structure.getEnd());
				} else {
					getMonitor().warn("Expected single structure element for field {} but got {} - ignoring", fieldName,
							pathStructures.size());
				}
			} catch (InvalidParameterException e) {
				getMonitor().warn("Failed to match structure for field " + fieldName, e);
			}
		}
	}

	/**
	 * Creates the field annotation.
	 *
	 * @param jCas
	 *            the JCas
	 * @param name
	 *            the name
	 * @param
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 */
	private void createFieldAnnotation(JCas jCas, String source, String name, int begin, int end) {
		TemplateField field = new TemplateField(jCas);
		field.setBegin(begin);
		field.setEnd(end);
		field.setName(name);
		field.setSource(source);
		addToJCasIndex(field);
	}

	/**
	 * Creates the record annotation.
	 *
	 * @param jCas
	 *            the JCas
	 * @param name
	 *            the name
	 * @param string
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 */
	private void createRecordAnnotation(JCas jCas, String source, String name, int begin, int end) {
		Record record = new Record(jCas);
		record.setBegin(begin);
		record.setSource(source);
		record.setEnd(end);
		record.setName(name);
		addToJCasIndex(record);
	}
}