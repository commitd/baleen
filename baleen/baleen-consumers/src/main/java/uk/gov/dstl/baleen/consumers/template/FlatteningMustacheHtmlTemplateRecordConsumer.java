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
		Map<String, Object> context = new HashMap<>();

		Map<String, String> flattenedFields = getFlattenedFields(records);
		context.put("fields", flattenedFields);

		Map<String, Map<String, String>> flattenedRecords = getFlattenedRecords(records);
		context.put("records", flattenedRecords);

		Map<String, Map<String, Map<String, String>>> sourceRecords = getSourceRecords(records);
		context.put("sources", sourceRecords);

		return context;
	}

	private static Map<String, String> getFlattenedFields(Map<String, Collection<ExtractedRecord>> records) {
		Map<String, String> fieldMap = new HashMap<>();
		for (Entry<String, Collection<ExtractedRecord>> entry : records.entrySet()) {
			Collection<ExtractedRecord> sourceRecords = entry.getValue();
			for (ExtractedRecord extractedRecord : sourceRecords) {
				Collection<ExtractedField> fields = extractedRecord.getFields();
				fields.forEach(field -> fieldMap.put(field.getName(), field.getValue()));
			}
		}
		return fieldMap;
	}

	private static Map<String, Map<String, String>> getFlattenedRecords(
			Map<String, Collection<ExtractedRecord>> records) {
		Map<String, Map<String, String>> recordMap = new HashMap<>();
		for (Entry<String, Collection<ExtractedRecord>> entry : records.entrySet()) {
			Collection<ExtractedRecord> sourceRecords = entry.getValue();
			for (ExtractedRecord extractedRecord : sourceRecords) {
				if (extractedRecord.getKind() == Kind.DEFAULT) {
					continue;
				}
				Collection<ExtractedField> fields = extractedRecord.getFields();
				Map<String, String> fieldMap = new HashMap<>();
				String name = extractedRecord.getName();
				fields.forEach(field -> fieldMap.put(field.getName(), field.getValue()));
				if (fieldMap.size() > 0) {
					recordMap.put(name, fieldMap);
				}
			}
		}
		return recordMap;
	}

	private static Map<String, Map<String, Map<String, String>>> getSourceRecords(
			Map<String, Collection<ExtractedRecord>> records) {
		Map<String, Map<String, Map<String, String>>> sourceMap = new HashMap<>();
		for (Entry<String, Collection<ExtractedRecord>> entry : records.entrySet()) {
			String sourceName = entry.getKey();
			Map<String, Map<String, String>> recordsMap = new HashMap<>();
			Collection<ExtractedRecord> sourceRecords = entry.getValue();
			for (ExtractedRecord extractedRecord : sourceRecords) {
				if (extractedRecord.getKind() == Kind.DEFAULT) {
					continue;
				}
				Collection<ExtractedField> fields = extractedRecord.getFields();
				Map<String, String> recordFields = new HashMap<>();
				recordsMap.put(extractedRecord.getName(), recordFields);
				fields.forEach(field -> recordFields.put(field.getName(), field.getValue()));
			}
			if (recordsMap.size() > 0) {
				sourceMap.put(sourceName, recordsMap);
			}
		}
		return sourceMap;
	}

}
