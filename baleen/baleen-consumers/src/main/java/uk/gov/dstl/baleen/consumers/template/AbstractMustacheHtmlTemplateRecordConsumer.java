package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Abstract base implementation of the Mustache HTML template record consumer.
 * 
 * Subclasses should implement {@link #mapFields(Map)} to convert from records
 * to template fields.
 */
public abstract class AbstractMustacheHtmlTemplateRecordConsumer extends AbstractRecordConsumer {

	/** The Constant PARAM_FILENAME. */
	public static final String PARAM_FILENAME = "templateFilename";

	/**
	 * The template filename to use.
	 *
	 * @baleen.config template.html
	 */
	@ConfigurationParameter(name = PARAM_FILENAME, defaultValue = "template.html")
	private String templateFilename;

	/** The Constant PARAM_OUTPUT_DIRECTORY. */
	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

	/**
	 * The output directory.
	 *
	 * @baleen.config generatedDocuments
	 */
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, defaultValue = "generatedDocuments")
	private String outputDirectory = "generatedDocuments";

	/** The template. */
	private Template template;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			compileTemplate();
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	/**
	 * Compile template.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void compileTemplate() throws IOException {
		Compiler compiler = Mustache.compiler();
		String templateHtml = new String(Files.readAllBytes(Paths.get(templateFilename)), StandardCharsets.UTF_8);
		template = compiler.compile(templateHtml);
	}

	@Override
	protected void writeRecords(JCas jCas, String documentSourceName, Map<String, Collection<ExtractedRecord>> records)
			throws AnalysisEngineProcessException {
		Map<String, ?> fields = mapFields(jCas, records);
		try (Writer writer = createOutputWriter(documentSourceName)) {
			template.execute(fields, writer);
		} catch (IOException e) {
			getMonitor().warn("Failed to process template " + templateFilename + " for " + documentSourceName, e);
		}
	}

	/**
	 * Map records to a moustache field name and value.
	 * 
	 * In trivial cases the field value may be a String, but in others it could
	 * be a list so the template can iterate the values.
	 *
	 * @param records
	 *            the records
	 * @return the map of field name to value
	 */
	protected abstract Map<String, ?> mapFields(JCas jCas, Map<String, Collection<ExtractedRecord>> records);

	/**
	 * Creates an output writer for a new file in the configured output
	 * directory, with appropriate name and ".html" extension.
	 * <p>
	 * Note: this overwrites existing files (warning if it does so).
	 * </p>
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
		Path outputFilePath = directoryPath.resolve(baseName + ".html");

		if (outputFilePath.toFile().exists()) {
			getMonitor().warn("Overwriting existing output properties file {}", outputFilePath);
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}
}
