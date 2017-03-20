package uk.gov.dstl.baleen.consumers.template;

import uk.gov.dstl.baleen.types.templates.RecordMarker;

public class RecordExtent {

	private final String name;

	private RecordMarker startAnnotation;

	private RecordMarker endAnnotation;

	public RecordExtent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public RecordMarker getStartAnnotation() {
		return startAnnotation;
	}

	public void setStartAnnotation(RecordMarker startAnnotation) {
		this.startAnnotation = startAnnotation;
	}

	public RecordMarker getEndAnnotation() {
		return endAnnotation;
	}

	public void setEndAnnotation(RecordMarker endAnnotation) {
		this.endAnnotation = endAnnotation;
	}

	@Override
	public String toString() {
		return "RecordExtent [name=" + name + ", startAnnotation=" + startAnnotation + ", endAnnotation="
				+ endAnnotation + "]";
	}

}
