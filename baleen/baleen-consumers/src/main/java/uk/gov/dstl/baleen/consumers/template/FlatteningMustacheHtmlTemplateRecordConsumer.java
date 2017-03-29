package uk.gov.dstl.baleen.consumers.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.consumers.template.ExtractedRecord.Kind;

public class FlatteningMustacheHtmlTemplateRecordConsumer extends AbstractMustacheHtmlTemplateRecordConsumer {

	@Override
	protected Map<String, ?> mapFields(JCas jCas, Map<String, Collection<ExtractedRecord>> records) {
		Map<String, String> values = new HashMap<>();
		for (Entry<String, Collection<ExtractedRecord>> entry : records.entrySet()) {
			String sourceName = entry.getKey();
			Collection<ExtractedRecord> sourceRecords = entry.getValue();
			for (ExtractedRecord extractedRecord : sourceRecords) {
				String recordPrefix = (Kind.DEFAULT == extractedRecord.getKind()) ? ""
						: extractedRecord.getName() + ".";
				Collection<ExtractedField> fields = extractedRecord.getFields();
				for (ExtractedField extractedField : fields) {
					String key = sourceName + "." + recordPrefix + extractedField.getName();
					String value = extractedField.getValue();
					values.put(key, value);
				}
			}
		}
		return values;
	}
}
