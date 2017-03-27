package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.reflections.Reflections;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import uk.gov.dstl.baleen.annotators.templates.FieldDefinitionConfiguration;
import uk.gov.dstl.baleen.annotators.templates.RecordAnnotator;
import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionAnnotator;
import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionConfiguration;
import uk.gov.dstl.baleen.annotators.templates.TemplateFieldDefinitionAnnotator;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.RecordDefinition;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

/**
 * Writes RecordDefinitions, and the TemplateFieldDefinitions that they cover,
 * to YAML files for subsequent use in {@link RecordAnnotator}.
 * <p>
 * See {@link RecordAnnotator} for a description of the format.
 * </p>
 *
 * <p>
 * This consumer should be used with {@link RecordDefinitionAnnotator} and
 * {@link TemplateFieldDefinitionAnnotator}.
 * </p>
 */
public class RecordDefinitionConfigurationCreatingConsumer extends BaleenConsumer {

	/** The Constant DEFAULT_STRUCTURAL_PACKAGE. */
	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	/**
	 * A list of structural types which will be considered during record path
	 * analysis.
	 *
	 * @baleen.config Paragraph,TableCell,ListItem,Aside, ...
	 */
	public static final String PARAM_TYPE_NAMES = "types";

	/** The type names. */
	@ConfigurationParameter(name = PARAM_TYPE_NAMES, mandatory = false)
	private String[] typeNames;

	/** The structural classes. */
	private Set<Class<? extends Structure>> structuralClasses;

	/** The Constant PARAM_OUTPUT_DIRECTORY. */
	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

	/** The output directory. */
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, defaultValue = "recordDefinitions")
	private String outputDirectory = "recordDefinitions";

	/** The object mapper. */
	private final ObjectMapper objectMapper;

	/**
	 * Instantiates a new record definition configuration creating consumer.
	 */
	public RecordDefinitionConfigurationCreatingConsumer() {
		objectMapper = new ObjectMapper(new YAMLFactory());
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		structuralClasses = new HashSet<>();
		if (typeNames == null || typeNames.length == 0) {
			Reflections reflections = new Reflections(DEFAULT_STRUCTURAL_PACKAGE);
			structuralClasses = reflections.getSubTypesOf(Structure.class);
		} else {
			for (final String typeName : typeNames) {
				try {
					structuralClasses.add(CpeBuilderUtils.getClassFromString(typeName, DEFAULT_STRUCTURAL_PACKAGE));
				} catch (final InvalidParameterException e) {
					throw new ResourceInitializationException(e);
				}
			}
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<RecordDefinitionConfiguration> recordConfigurations = new ArrayList<>();

		Collection<RecordDefinition> recordDefinitions = JCasUtil.select(jCas, RecordDefinition.class);
		Set<TemplateFieldDefinition> allFieldDefinitions = new HashSet<>(
				JCasUtil.select(jCas, TemplateFieldDefinition.class));

		for (RecordDefinition recordDefinition : recordDefinitions) {
			List<Structure> precedingStructure = JCasUtil.selectPreceding(Structure.class, recordDefinition, 1);
			List<Structure> followingStructure = JCasUtil.selectFollowing(Structure.class, recordDefinition, 1);

			if (precedingStructure.size() != 1 || followingStructure.size() != 1) {
				getMonitor().warn(
						"Could not find preceeding or following structure elements for record definition {} - giving up",
						recordDefinition.getName());
				continue;
			}

			String precedingPath = SelectorUtils.generatePath(jCas, precedingStructure.iterator().next(),
					structuralClasses);
			String followingPath = SelectorUtils.generatePath(jCas, followingStructure.iterator().next(),
					structuralClasses);

			List<TemplateFieldDefinition> definitions = JCasUtil.selectCovered(TemplateFieldDefinition.class,
					recordDefinition);
			allFieldDefinitions.removeAll(definitions);

			List<FieldDefinitionConfiguration> fields = makeFields(jCas, definitions);
			recordConfigurations.add(new RecordDefinitionConfiguration(recordDefinition.getName(), precedingPath,
					followingPath, fields));
		}

		if (!allFieldDefinitions.isEmpty()) {
			recordConfigurations.add(new RecordDefinitionConfiguration(makeFields(jCas, allFieldDefinitions)));
		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());
		try (Writer w = createOutputWriter(documentSourceName)) {
			objectMapper.writeValue(w, recordConfigurations);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Make field selector name/path maps
	 *
	 * @param jCas
	 *            the jCas
	 * @param fields
	 *            the fields
	 * @return the map of name/path pairs
	 */
	private List<FieldDefinitionConfiguration> makeFields(final JCas jCas,
			Collection<TemplateFieldDefinition> definitions) {
		List<FieldDefinitionConfiguration> fields = new ArrayList<>();

		for (TemplateFieldDefinition templateFieldDefinition : definitions) {
			String fieldPath = SelectorUtils.generatePath(jCas, templateFieldDefinition, structuralClasses);
			FieldDefinitionConfiguration field = new FieldDefinitionConfiguration(templateFieldDefinition.getName(),
					fieldPath);
			field.setRegex(templateFieldDefinition.getRegex());
			fields.add(field);
		}
		return fields;
	}

	/**
	 * Creates the output writer for the configuration yaml files.
	 *
	 * @param documentSourceName
	 *            the document source name
	 * @return the writer
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private Writer createOutputWriter(final String documentSourceName) throws IOException {
		Path directoryPath = Paths.get(outputDirectory);
		if (!directoryPath.toFile().exists()) {
			Files.createDirectories(directoryPath);
		}
		String baseName = FilenameUtils.getBaseName(documentSourceName);
		Path outputFilePath = directoryPath.resolve(baseName + ".yaml");

		if (outputFilePath.toFile().exists()) {
			getMonitor().warn("Overwriting existing output properties file {}", outputFilePath);
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

}
