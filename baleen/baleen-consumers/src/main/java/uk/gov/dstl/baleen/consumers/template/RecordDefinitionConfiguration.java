package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class RecordDefinitionConfiguration {

	private String name;

	private Map<String, String> fieldPaths;

	public RecordDefinitionConfiguration() {
		// for reflective construction in Jackson
	}

	public RecordDefinitionConfiguration(String name, Map<String, String> fieldPaths) {
		this.name = name;
		this.fieldPaths = fieldPaths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getFieldPaths() {
		return fieldPaths;
	}

	public void setFieldPaths(Map<String, String> fieldPaths) {
		this.fieldPaths = fieldPaths;
	}

}
