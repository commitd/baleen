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
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

public class RecordConsumer extends BaleenConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordConsumer.class);

	public static final String PARAM_OUTPUT_FILE = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, defaultValue = "records")
	private String outputDirectory = "records";

	public static final String PARAM_OUTPUT_FORMAT = "outputFormat";
	@ConfigurationParameter(name = PARAM_OUTPUT_FORMAT, defaultValue = "yaml")
	private String outputFormat = "yaml";

	private ObjectMapper objectMapper;

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		if ("json".equals(outputFormat)) {
			objectMapper = new ObjectMapper();
		} else {
			objectMapper = new ObjectMapper(new YAMLFactory());
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<ExtractedRecord> records = new ArrayList<>();
		Collection<Record> recordAnnotations = JCasUtil.select(jCas, Record.class);
		for (Record recordAnnotation : recordAnnotations) {
			Collection<TemplateField> fieldAnnotations = JCasUtil.selectCovered(TemplateField.class, recordAnnotation);
			Map<String, String> fieldValues = new HashMap<>();
			for (TemplateField templateField : fieldAnnotations) {
				fieldValues.put(templateField.getName(), templateField.getCoveredText());
			}
			records.add(new ExtractedRecord(recordAnnotation.getName(), fieldValues));
		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());
		try (Writer w = createOutputWriter(documentSourceName)) {
			objectMapper.writeValue(w, records);
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