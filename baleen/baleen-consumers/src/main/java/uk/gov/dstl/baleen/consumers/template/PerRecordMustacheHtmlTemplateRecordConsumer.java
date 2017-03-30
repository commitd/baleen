package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.samskivert.mustache.Template;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

public class PerRecordMustacheHtmlTemplateRecordConsumer extends AbstractMustacheHtmlTemplateRecordConsumer {

	/** The Constant PARAM_RECORD_TEMPLATE_DIRECTORY. */
	public static final String PARAM_RECORD_TEMPLATE_DIRECTORY = "recordTemplateDirectory";

	/**
	 * A directory containing templates named after each desired output record.
	 *
	 * @baleen.config recordTemplates
	 */
	@ConfigurationParameter(name = PARAM_RECORD_TEMPLATE_DIRECTORY, defaultValue = "recordTemplates")
	private String recordTemplateDirectory;

	/** The templates. */
	private Map<String, Template> templates;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		templates = new HashMap<>();
		Path templatesDir = Paths.get(recordTemplateDirectory);
		try (Stream<Path> templateStream = Files.list(templatesDir)) {
			templateStream.forEach(this::compileAndStoreTemplate);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	private void compileAndStoreTemplate(Path templatePath) {
		try {
			Template template = compileTemplate(templatePath);
			String baseName = FilenameUtils.getBaseName(templatePath.toString());
			templates.put(baseName, template);
		} catch (IOException e) {
			getMonitor().warn("Failed to compile template " + templatePath.toAbsolutePath().toString(), e);
		}
	}

	@Override
	protected void writeRecords(String documentSourceName, Map<String, Collection<ExtractedRecord>> records,
			Map<String, Object> fieldMap) {
		for (Collection<ExtractedRecord> extractedRecords : records.values()) {
			for (ExtractedRecord extractedRecord : extractedRecords) {
				String name = extractedRecord.getName();
				Template template = templates.get(name);
				if (template == null) {
					getMonitor().info("No template found for record {}", name);
					continue;
				}
				try (Writer writer = createOutputWriter(documentSourceName, name)) {
					template.execute(fieldMap, writer);
				} catch (IOException e) {
					getMonitor().warn("Failed to write record " + name + " for document " + documentSourceName, e);
				}
			}
		}
	}
}
