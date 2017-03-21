package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.reflections.Reflections;
import uk.gov.dstl.baleen.annotators.templates.TemplateFieldDefinitionAnnotator;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

/**
 * Writes {@link Properties} files from TemplateFieldDefinition annotations
 * containing field names and the structural selector paths to extract them.
 * <p>
 * See {@link SelectorUtils}.
 * </p>
 * <p>
 * This consumer should be used with {@link TemplateFieldDefinitionAnnotator}.
 * <p>
 */
public class TemplateSelectorCreatingConsumer extends BaleenConsumer {

	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	/**
	 * A list of structural types which will be considered during template path
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
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, defaultValue = "templateSelectors")
	private String outputDirectory = "templateSelectors";

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		if (typeNames == null || typeNames.length == 0) {
			Reflections reflections = new Reflections(DEFAULT_STRUCTURAL_PACKAGE);
			structuralClasses = reflections.getSubTypesOf(Structure.class);
		} else {
			structuralClasses = new HashSet<>();
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
		final Properties properties = new Properties();
		final Collection<TemplateFieldDefinition> templateFields = JCasUtil.select(jCas, TemplateFieldDefinition.class);

		for (TemplateFieldDefinition templateField : templateFields) {
			properties.put(templateField.getName(), SelectorUtils.generatePath(jCas, templateField, structuralClasses));
		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());

		try (Writer w = createOutputWriter(documentSourceName)) {
			properties.store(w, "Template Selectors - generated from " + documentSourceName);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Creates the output writer.
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
		Path outputFilePath = directoryPath.resolve(baseName + ".properties");

		if (outputFilePath.toFile().exists()) {
			getMonitor().warn("Overwriting existing output properties file {}", outputFilePath);
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

}