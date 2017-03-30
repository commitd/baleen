package uk.gov.dstl.baleen.annotators.templates;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;

/**
 * Using previously created record and template definitions and annotations,
 * remove records that are not valid.
 *
 * <p>
 * Each YAML configuration file for records can contain multiple definitions for
 * records. Template fields can be considered required to make a record valid.
 * </p>
 *
 * <p>
 * This annotator (or cleaner) removes records which do not contain all required
 * fields.
 * </p>
 *
 * <p>
 * This can be configured to only remove invalid records of specified types by
 * supplying a list of record definition names.
 * </p>
 */
public class RecordValidator extends AbstractRecordAnnotator {

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<Record> recordAnnotations = JCasUtil.select(jCas, Record.class);
		for (Entry<String, RecordDefinitionConfiguration> entry : recordDefinitions.entries()) {
			doProcessRecordDefinition(entry.getKey(), entry.getValue(), recordAnnotations);
		}
	}

	/**
	 *
	 * Removes invalid records.
	 *
	 * @param source
	 *            the source
	 * @param recordDefinition
	 *            the RecordDefinitionConfiguration
	 * @param recordAnnotations
	 *            the record annotations
	 */
	protected void doProcessRecordDefinition(String source, RecordDefinitionConfiguration recordDefinition,
			Collection<Record> recordAnnotations) {

		getRecordsForRecordDefinition(recordAnnotations, source, recordDefinition.getName()).forEach(r -> {

			Collection<TemplateField> fieldAnnotations = getTemplateFieldsForRecord(source, r);
			Set<String> fieldsPresent = getNamesOfFieldsPresent(fieldAnnotations);

			Optional<String> missingRequired = streamNamesOfRequiredFields(recordDefinition)
					.filter(required -> !fieldsPresent.contains(required)).findFirst();

			if (missingRequired.isPresent()) {
				getMonitor().info("Removing invalid record {} - {} from as missing require field {}", source,
						recordDefinition.getName(), missingRequired.get());
				removeFromJCasIndex(r);
				removeFromJCasIndex(fieldAnnotations);
			}
		});

	}

	/**
	 * Stream the names of the required fields for the given record definition.
	 *
	 * @param recordDefinition
	 *            the record definition
	 * @return stream of the names of the require fields
	 */
	private Stream<String> streamNamesOfRequiredFields(RecordDefinitionConfiguration recordDefinition) {
		return recordDefinition.getFields().stream().filter(FieldDefinitionConfiguration::isRequired)
				.map(FieldDefinitionConfiguration::getName);
	}

	/**
	 * Get the names of the fields given
	 *
	 * @param fieldAnnotations
	 *            the field annotations
	 * @return the names of the given fields
	 */
	private Set<String> getNamesOfFieldsPresent(Collection<TemplateField> fieldAnnotations) {
		return fieldAnnotations.stream().map(TemplateField::getName).collect(Collectors.toSet());
	}

	/**
	 * Get the template fields for the given source and record.
	 *
	 * @param source
	 *            the source
	 * @param record
	 *            the record
	 * @return
	 */
	private Collection<TemplateField> getTemplateFieldsForRecord(String source, Record record) {
		return JCasUtil.selectCovered(TemplateField.class, record).stream().filter(t -> source.equals(t.getSource()))
				.collect(Collectors.toList());
	}

	/**
	 * Get the records for the given source and record definition name
	 *
	 * @param records
	 *            all the records
	 * @param source
	 *            the source
	 * @param name
	 *            the name of the record
	 * @return a stream of the records
	 */
	private Stream<Record> getRecordsForRecordDefinition(Collection<Record> records, String source, String name) {
		return records.stream().filter(r -> source.equals(r.getSource()) && name.equals(r.getName()));
	}

}