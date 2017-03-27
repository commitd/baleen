package uk.gov.dstl.baleen.consumers.template;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MongoExtractedRecords {

	private String externalId;

	private String sourceUri;

	private Map<String, Collection<ExtractedRecord>> records;

	public MongoExtractedRecords() {
		// for reflective construction in Jackson
	}

	public MongoExtractedRecords(String id, String sourceUri, Map<String, Collection<ExtractedRecord>> records) {
		this.externalId = id;
		this.sourceUri = sourceUri;
		this.records = records;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public void setSourceUri(String sourceUri) {
		this.sourceUri = sourceUri;
	}

	public Map<String, Collection<ExtractedRecord>> getRecords() {
		return records;
	}

	public void setRecords(Map<String, Collection<ExtractedRecord>> records) {
		this.records = records;
	}

}
