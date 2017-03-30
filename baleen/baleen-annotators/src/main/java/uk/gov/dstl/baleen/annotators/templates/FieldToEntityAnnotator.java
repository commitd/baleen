package uk.gov.dstl.baleen.annotators.templates;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.templates.Record;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;
import uk.gov.dstl.baleen.uima.utils.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

/**
 * Creates a new Entity of the configured type for each field of a given name in
 * each record with a given name.
 * 
 * Optionally, a source can be provided to disambiguate records/fields created
 * from multiple definition configurations.
 * 
 * Example configuration:
 * 
 * <pre>
... 
annotators:
- class templates.RecordAnnotator:
  ...
- class templates.FieldToEntityAnnotator:
  entityType: Person
  recordName: report
  fieldName: athlete
  source: athleteReportDefinitions
 * </pre>
 * 
 */
public class FieldToEntityAnnotator extends BaleenAnnotator {

	/** The Constant PARAM_ENTITY_TYPE. */
	public static final String PARAM_ENTITY_TYPE = "entityType";

	/**
	 * The entity type to create.
	 * 
	 * @baleen.config semantic.Entity
	 */
	@ConfigurationParameter(name = PARAM_ENTITY_TYPE, mandatory = true)
	private String entityType;

	/** The Constant PARAM_RECORD_NAME. */
	public static final String PARAM_RECORD_NAME = "recordName";

	/**
	 * The record type to search for the field.
	 * 
	 * @baleen.config record
	 */
	@ConfigurationParameter(name = PARAM_RECORD_NAME, mandatory = true)
	private String recordName;

	/** The Constant PARAM_FIELD_NAME. */
	public static final String PARAM_FIELD_NAME = "fieldName";

	/**
	 * The field name to find.
	 * 
	 * @baleen.config field
	 */
	@ConfigurationParameter(name = PARAM_FIELD_NAME, mandatory = true)
	private String fieldName;

	/** The Constant PARAM_SOURCE. */
	public static final String PARAM_SOURCE = "source";

	/**
	 * The source type to search for the record.
	 */
	@ConfigurationParameter(name = PARAM_SOURCE, mandatory = false)
	private String source;

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		Collection<Record> records = JCasUtil.select(jCas, Record.class);
		for (Record record : records) {
			if (!StringUtils.equals(recordName, record.getName())
					|| (!StringUtils.isEmpty(source) && !source.equalsIgnoreCase(record.getSource()))) {
				continue;
			}

			List<TemplateField> fields = JCasUtil.selectCovered(TemplateField.class, record);
			for (TemplateField field : fields) {
				if (!StringUtils.equals(fieldName, field.getName())) {
					continue;
				}
				try {
					createEntity(jCas, field);
				} catch (BaleenException e) {
					getMonitor().warn("Failed to process entity for record " + recordName + " field " + fieldName, e);
				}
			}
		}
	}

	/**
	 * Creates a new entity of the configured type, setting the value to the
	 * covered text of the matched template field.
	 *
	 * @param jCas
	 *            the jCas
	 * @param field
	 *            the field
	 * @throws BaleenException
	 *             the baleen exception
	 */
	private void createEntity(JCas jCas, TemplateField field) throws BaleenException {
		Class<? extends Entity> type = TypeUtils.getEntityClass(entityType, jCas);
		try {
			Constructor<? extends Entity> constructor = type.getConstructor(JCas.class);
			Entity entity = constructor.newInstance(jCas);
			entity.setBegin(field.getBegin());
			entity.setEnd(field.getEnd());
			entity.setValue(field.getCoveredText());
			addToJCasIndex(entity);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new BaleenException("Failed to create entity of type " + entityType, e);
		}
	}

}
