package uk.gov.dstl.baleen.consumers.template;

import java.util.Map;

public class ExtractedRecord {

	public enum Kind {
		NAMED, DEFAULT
	}

	public String name;

	public Kind kind;

	public Map<String, String> fields;

	public ExtractedRecord() {
		// for reflective construction in Jackson
	}

	public ExtractedRecord(String name, Map<String, String> fields) {
		this.name = name;
		this.kind = Kind.NAMED;
		this.fields = fields;
	}

	public ExtractedRecord(Map<String, String> fields) {
		this.kind = Kind.DEFAULT;
		this.fields = fields;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public Map<String, String> getFields() {
		return fields;
	}

	public void setFields(Map<String, String> fields) {
		this.fields = fields;
	}

}
