package uk.gov.dstl.baleen.consumers.template;

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
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.annotators.templates.RecordAnnotator;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

/**
 * Writes Records, and the TemplateFields covered by them, to YAML or JSON
 * files.
 * <p>
 * Each entry in the file is an "object" with a <code>kind</code> field of
 * <code>NAMED</code> or <code>DEFAULT</code> and a <code>fields</code> field
 * consisting of a dictionary / map of name and value pairs from the
 * TemplateField annotations. In the case of <code>NAMED</code> records, there
 * will be an additional <code>name</code> field.
 * </p>
 * <p>
 * The output format defaults to YAML, but can be changed to JSON by setting the
 * configuration parameter <code>outputFormat</code> to <code>json</code> (all
 * other values will result in YAML output).
 * </p>
 * <p>
 * This consumer should be used with {@link RecordAnnotator}.
 * </p>
 */
public class RecordConsumer extends BaleenConsumer {

	/** The Constant PARAM_OUTPUT_DIRECTORY. */
	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

	/** The output directory. */
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, defaultValue = "records")
	private String outputDirectory = "records";

	/** The Constant PARAM_OUTPUT_FORMAT. */
	public static final String PARAM_OUTPUT_FORMAT = "outputFormat";

	/** The output format. */
	@ConfigurationParameter(name = PARAM_OUTPUT_FORMAT, defaultValue = "yaml")
	private String outputFormat = "yaml";

	/** The object mapper, used for serialising Records */
	private ObjectMapper objectMapper;

	/** The extension to use for output files */
	private String outputFileExtension;

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		if ("json".equals(outputFormat)) {
			objectMapper = new ObjectMapper();
			outputFileExtension = "json";
		} else {
			objectMapper = new ObjectMapper(new YAMLFactory());
			outputFileExtension = "yaml";
		}
		objectMapper.setSerializationInclusion(Include.NON_NULL);
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<ExtractedRecord> records = new ArrayList<>();
		Collection<Record> recordAnnotations = JCasUtil.select(jCas, Record.class);

		HashSet<TemplateField> allFields = new HashSet<>(JCasUtil.select(jCas, TemplateField.class));

		for (Record recordAnnotation : recordAnnotations) {
			Collection<TemplateField> fieldAnnotations = JCasUtil.selectCovered(TemplateField.class, recordAnnotation);
			allFields.removeAll(fieldAnnotations);
			Map<String, String> fieldValues = makeFieldValues(fieldAnnotations);
			records.add(new ExtractedRecord(recordAnnotation.getName(), fieldValues));
		}

		if (!allFields.isEmpty()) {
			records.add(new ExtractedRecord(makeFieldValues(allFields)));
		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());
		try (Writer w = createOutputWriter(documentSourceName)) {
			objectMapper.writeValue(w, records);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	/**
	 * Makes the field name/value pairs from a collection of field annotations.
	 *
	 * @param fieldAnnotations
	 *            the field annotations
	 * @return the field value name/value pairs
	 */
	private static Map<String, String> makeFieldValues(Collection<TemplateField> fieldAnnotations) {
		Map<String, String> fieldValues = new HashMap<>();
		for (TemplateField templateField : fieldAnnotations) {
			fieldValues.put(templateField.getName(), templateField.getCoveredText());
		}
		return fieldValues;
	}

	/**
	 * Creates an output writer for a new file in the configured output
	 * directory, with appropriate name and extension.
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
		Path outputFilePath = directoryPath.resolve(baseName + "." + outputFileExtension);

		if (outputFilePath.toFile().exists()) {
			getMonitor().warn("Overwriting existing output properties file {}", outputFilePath);
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

}