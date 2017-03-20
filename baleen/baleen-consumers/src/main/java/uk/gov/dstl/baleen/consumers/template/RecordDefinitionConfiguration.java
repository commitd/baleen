package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class RecordDefinitionConfiguration {

	private String name;

	private Map<String, String> fieldPaths;

	private String precedingPath;

	private String followingPath;

	public RecordDefinitionConfiguration() {
		// for reflective construction in Jackson
	}

	public RecordDefinitionConfiguration(String name, String precedingPath, String followingPath,
			Map<String, String> fieldPaths) {
		this.name = name;
		this.precedingPath = precedingPath;
		this.followingPath = followingPath;
		this.fieldPaths = fieldPaths;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrecedingPath() {
		return precedingPath;
	}

	public void setPrecedingPath(String precedingPath) {
		this.precedingPath = precedingPath;
	}

	public String getFollowingPath() {
		return followingPath;
	}

	public void setFollowingPath(String followingPath) {
		this.followingPath = followingPath;
	}

	public Map<String, String> getFieldPaths() {
		return fieldPaths;
	}

	public void setFieldPaths(Map<String, String> fieldPaths) {
		this.fieldPaths = fieldPaths;
	}

}
