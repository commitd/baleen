package uk.gov.dstl.baleen.consumers.template;

import uk.gov.dstl.baleen.types.templates.Record;

public class RecordExtent {

	private final String name;

	private Record startAnnotation;

	private Record endAnnotation;

	public RecordExtent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Record getStartAnnotation() {
		return startAnnotation;
	}

	public void setStartAnnotation(Record startAnnotation) {
		this.startAnnotation = startAnnotation;
	}

	public Record getEndAnnotation() {
		return endAnnotation;
	}

	public void setEndAnnotation(Record endAnnotation) {
		this.endAnnotation = endAnnotation;
	}

	@Override
	public String toString() {
		return "RecordExtent [name=" + name + ", startAnnotation=" + startAnnotation + ", endAnnotation="
				+ endAnnotation + "]";
	}

}
