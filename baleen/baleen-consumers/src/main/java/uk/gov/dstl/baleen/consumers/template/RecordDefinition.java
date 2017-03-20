package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class RecordDefinition {

	private String name;

	private String recordBeginPath;

	private Map<String, String> fieldPaths;

	public RecordDefinition() {
		// for reflective construction in Jackson
	}

	public RecordDefinition(String name, String recordBeginPath, Map<String, String> fieldPaths) {
		this.name = name;
		this.recordBeginPath = recordBeginPath;
		this.fieldPaths = fieldPaths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRecordBeginPath() {
		return recordBeginPath;
	}

	public void setRecordBeginPath(String recordBeginPath) {
		this.recordBeginPath = recordBeginPath;
	}

	public Map<String, String> getFieldPaths() {
		return fieldPaths;
	}

	public void setFieldPaths(Map<String, String> fieldPaths) {
		this.fieldPaths = fieldPaths;
	}

}
