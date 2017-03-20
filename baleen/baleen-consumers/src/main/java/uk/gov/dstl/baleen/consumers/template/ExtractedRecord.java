package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class ExtractedRecord {
	public String name;

	public Map<String, String> fields;

	public ExtractedRecord() {
		// for reflective construction in Jackson
	}

	public ExtractedRecord(String name, Map<String, String> fields) {
		this.name = name;
		this.fields = fields;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

}
