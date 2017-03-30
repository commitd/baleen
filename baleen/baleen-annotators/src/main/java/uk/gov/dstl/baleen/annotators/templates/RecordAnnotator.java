package uk.gov.dstl.baleen.annotators.templates;

import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.templates.RecordDefinitionConfiguration.Kind;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

/**
 * Using previously created record definitions, creates annotations for records
 * and the the fields contained within them.
 *
 * <p>
 * Each YAML configuration file contains multiple definitions in an array/list,
 * with each definition being an object with following fields:
 * <p>
 * <dl>
 * <dt>fields</dt>
 * <dd>A list of field definitions. Fields must have a <code>name</code> and
 * <code>path</code>, and can optionally have a regular expression
 * (<code>regex</code>) a <code>defaultValue</code> and declare if they are
 * <code>required</code>. A TemplateField annotation is created for each matched
 * path and restrictions.</dd>
 *
 * <dt>kind</dt>
 * <dd>Whether the field selectors above should be used to create a
 * <code>NAMED</code> record, in which case a name field will also be supplied,
 * or these are not part of an explicit record, and thus gathered into a
 * <code>DEFAULT</code> record, so they are still annotated as
 * TemplateFields.</dd>
 * <dt>name</dt>
 * <dd>Only present on <code>NAMED</code> RecordDefinitions, and is populated
 * with the name of the record.
 * <dd>
 * </dl>
 *
 * An example YAML configuration could be:
 *
 * <pre>
---
- name: "NamedRecord"
  kind: "NAMED"
  fields:
    - name: "Description"
      path: "Paragraph:nth-of-type(8)"
    - name: "FullName"
      path: "Table:nth-of-type(2) > TableBody > TableRow:nth-of-type(2) >\
      \ TableCell:nth-of-type(2) > Paragraph"
      required: "true"
  precedingPath: "Paragraph:nth-of-type(6)"
  followingPath: "Paragraph:nth-of-type(10)"
- kind: "DEFAULT"
  fields:
    - name: "DocumentTitle"
      path: "Heading:nth-of-type(2)"
    - name: "DocumentDate"
      path: "Paragraph:nth-of-type(3)"
      regex: "\d{1,2}\/\d{1,2}\/\d{4}"
 * </pre>
 * <p>
 * Configurations are typically created by running a pipeline with the
 * RecordDefinitionConfigurationCreatingConsumer, which uses annotations created
 * by RecordDefinitionAnnotation and TemplateFieldDefinitionAnnotator running
 * over template documents.
 * </p>
 */
public class RecordAnnotator extends AbstractRecordAnnotator {

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		for (Entry<String, RecordDefinitionConfiguration> entry : recordDefinitions.entries()) {
			doProcessRecordDefinition(jCas, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Process for the given source and record definition. The passed JCas
	 * object contains information about the document and any existing
	 * annotations.
	 *
	 * @param jCas
	 *            JCas object to process
	 * @param source
	 *            the source of this record definition
	 * @param recordDefinition
	 *            the recordDefinition
	 * @throws AnalysisEngineProcessException
	 */
	protected void doProcessRecordDefinition(final JCas jCas, String source,
			RecordDefinitionConfiguration recordDefinition) {
		createTemplateFields(source, recordDefinition.getFields(), jCas);
		if (recordDefinition.getKind() == Kind.NAMED) {
			createRecord(source, recordDefinition, jCas);
		}
	}

	/**
	 * Creates the record based on the paths in the record definition.
	 *
	 * If errors occur during selection these are logged.
	 *
	 * @param source
	 *
	 * @param recordDefinition
	 *            the record definition
	 * @param jCas
	 *            the jCas
	 */
	private void createRecord(String source, RecordDefinitionConfiguration recordDefinition, JCas jCas) {
		Structure preceding = null;
		try {
			String preceedingPath = recordDefinition.getPrecedingPath();
			List<? extends Structure> precedingStructure = SelectorUtils.select(jCas, preceedingPath,
					DEFAULT_STRUCTURAL_PACKAGE);
			if (precedingStructure.size() == 1) {
				preceding = precedingStructure.iterator().next();
			}
		} catch (InvalidParameterException e) {
			getMonitor().warn("Failed to select structure preceeding record " + recordDefinition.getName(), e);
			return;
		}

		Structure following = null;
		try {
			String followingPath = recordDefinition.getFollowingPath();
			List<? extends Structure> followingStructure = SelectorUtils.select(jCas, followingPath,
					DEFAULT_STRUCTURAL_PACKAGE);
			if (followingStructure.size() == 1) {
				following = followingStructure.iterator().next();
			}
		} catch (InvalidParameterException e) {
			getMonitor().warn("Failed to select structure preceeding record " + recordDefinition.getName(), e);
			return;
		}

		if (preceding == null || following == null) {
			return;
		}

		createRecordAnnotation(jCas, source, recordDefinition.getName(), preceding.getEnd(), following.getBegin());
	}

	/**
	 * Creates the template fields based on the field definition selectors in
	 * the record definition.
	 *
	 * @param jCas
	 *            the jCas
	 * @param fieldPaths
	 *            the field paths
	 */
	private void createTemplateFields(String source, List<FieldDefinitionConfiguration> fields, JCas jCas) {
		for (FieldDefinitionConfiguration field : fields) {
			String path = field.getPath();
			String fieldName = field.getName();
			try {
				List<? extends Structure> pathStructures = SelectorUtils.select(jCas, path, DEFAULT_STRUCTURAL_PACKAGE);
				if (pathStructures.size() == 1) {
					Structure structure = pathStructures.get(0);
					createFieldAnnotation(jCas, source, field, structure);
				} else {
					getMonitor().warn("Expected single structure element for field {} but got {} - ignoring", fieldName,
							pathStructures.size());
				}
			} catch (InvalidParameterException e) {
				getMonitor().warn("Failed to match structure for field " + fieldName, e);
			}
		}
	}

	/**
	 * Create field annotation for the given field definition and matched
	 * structural element.
	 *
	 * @param jCas
	 *            the jCas
	 * @param source
	 *            the source template definition file name
	 * @param field
	 *            the field
	 * @param structure
	 *            the structure
	 */
	private void createFieldAnnotation(JCas jCas, String source, FieldDefinitionConfiguration field,
			Structure structure) {

		String defaultValue = field.getDefaultValue();

		if (structure.getCoveredText().isEmpty()) {
			if (field.isRequired() && defaultValue == null) {
				getMonitor().info("Required field missing {} in {}", field.getName(), source);
				return;
			} else {
				createFieldAnnotation(jCas, source, field.getName(), structure.getBegin(), structure.getEnd(),
						defaultValue);
			}
		}

		String regex = field.getRegex();

		if (regex == null) {
			createFieldAnnotation(jCas, source, field.getName(), structure.getBegin(), structure.getEnd(),
					structure.getCoveredText());
		} else {
			Pattern pattern = Pattern.compile(regex);
			String coveredText = structure.getCoveredText();
			Matcher matcher = pattern.matcher(coveredText);
			if (matcher.find()) {
				createFieldAnnotation(jCas, source, field.getName(), structure.getBegin() + matcher.start(),
						structure.getBegin() + matcher.end(), matcher.group());
			} else if (defaultValue != null) {
				getMonitor().info("Failed to match pattern {} in {} - using default value {}", regex, coveredText,
						defaultValue);
				createFieldAnnotation(jCas, source, field.getName(), structure.getBegin(), structure.getBegin(),
						defaultValue);
			} else {
				getMonitor().warn("Failed to match pattern {} in {} - ignoring", regex, coveredText);
			}
		}

	}

}