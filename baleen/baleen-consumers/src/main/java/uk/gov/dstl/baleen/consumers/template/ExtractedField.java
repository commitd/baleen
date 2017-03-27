package uk.gov.dstl.baleen.consumers.template;

public class ExtractedField {

	private String name;

	private String value;

	public ExtractedField() {
		// for reflective construction in Jackson
	}

	public ExtractedField(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
