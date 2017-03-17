package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class RecordDefinition {

	private String name;

	private String recordPath;

	private Map<String, String> fieldPaths;

	public RecordDefinition(String name, String recordPath, Map<String, String> fieldPaths) {
		this.name = name;
		this.recordPath = recordPath;
		this.fieldPaths = fieldPaths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRecordPath() {
		return recordPath;
	}

	public void setRecordPath(String recordPath) {
		this.recordPath = recordPath;
	}

	public Map<String, String> getFieldPaths() {
		return fieldPaths;
	}

	public void setFieldPaths(Map<String, String> fieldPaths) {
		this.fieldPaths = fieldPaths;
	}

}
