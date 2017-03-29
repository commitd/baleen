package uk.gov.dstl.baleen.consumers.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.consumers.template.ExtractedRecord.Kind;

public class FlatteningMustacheHtmlTemplateRecordConsumer extends AbstractMustacheHtmlTemplateRecordConsumer {

	@ConfigurationParameter(name = PARAM_FLATTEN_SOURCES, defaultValue = "false")
	private boolean flattenSources;
	public static final String PARAM_FLATTEN_SOURCES = "flattenSources";

	@ConfigurationParameter(name = PARAM_FLATTEN_RECORDS, defaultValue = "false")
	private boolean flattenRecords;
	public static final String PARAM_FLATTEN_RECORDS = "flattenRecords";

	@Override
	protected Map<String, ?> mapFields(JCas jCas, Map<String, Object> metadataMap,
			Map<String, Collection<ExtractedRecord>> records) {
		Map<String, String> values = new HashMap<>();
		for (Entry<String, Collection<ExtractedRecord>> entry : records.entrySet()) {
			String sourceName = entry.getKey();
			Collection<ExtractedRecord> sourceRecords = entry.getValue();
			for (ExtractedRecord extractedRecord : sourceRecords) {
				Collection<ExtractedField> fields = extractedRecord.getFields();
				for (ExtractedField extractedField : fields) {
					String key = makeKey(sourceName, extractedRecord, extractedField);
					String value = extractedField.getValue();
					values.put(key, value);
				}
			}
		}
		return values;
	}

	private String makeKey(String sourceName, ExtractedRecord extractedRecord, ExtractedField extractedField) {
		StringBuilder keyBuilder = new StringBuilder();

		if (!flattenSources) {
			keyBuilder.append(sourceName);
			keyBuilder.append('.');
		}

		if (!flattenRecords && (Kind.DEFAULT != extractedRecord.getKind())) {
			keyBuilder.append(extractedRecord.getName());
			keyBuilder.append('.');
		}

		keyBuilder.append(extractedField.getName());

		return keyBuilder.toString();
	}
}
