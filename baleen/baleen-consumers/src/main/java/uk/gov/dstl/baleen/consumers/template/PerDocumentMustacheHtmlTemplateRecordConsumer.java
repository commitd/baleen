package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import com.samskivert.mustache.Template;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

public class PerDocumentMustacheHtmlTemplateRecordConsumer extends AbstractMustacheHtmlTemplateRecordConsumer {

	/** The Constant PARAM_FILENAME. */
	public static final String PARAM_FILENAME = "templateFilename";

	/**
	 * The template filename to use.
	 *
	 * @baleen.config template.html
	 */
	@ConfigurationParameter(name = PARAM_FILENAME, defaultValue = "template.html")
	private String templateFilename;

	/** The template. */
	private Template template;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			template = compileTemplate(Paths.get(templateFilename));
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	protected void writeRecords(String documentSourceName, Map<String, Collection<ExtractedRecord>> records,
			Map<String, Object> fieldMap) {
		try (Writer writer = createOutputWriter(documentSourceName)) {
			template.execute(fieldMap, writer);
		} catch (IOException e) {
			getMonitor().warn("Failed to process template " + templateFilename + " for " + documentSourceName, e);
		}
	}

}
