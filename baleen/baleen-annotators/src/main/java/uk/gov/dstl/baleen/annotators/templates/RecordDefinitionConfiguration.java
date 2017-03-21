package uk.gov.dstl.baleen.annotators.templates;

import java.util.Map;

/**
 * A simple bean used to define Records and the fields contained within them.
 * 
 */
public class RecordDefinitionConfiguration {

	/**
	 * The Kind of record configuration (marker to indicate name and paths may
	 * be null)
	 */
	public enum Kind {

		/** Explicitly named record. */
		NAMED,
		/**
		 * Default record to capture all fields not explicitly covered by a
		 * record.
		 */
		DEFAULT
	}

	/** The record name. */
	private String name;

	/** The kind of record. */
	private Kind kind;

	/** The field paths. */
	private Map<String, String> fieldPaths;

	/** The element preceding the record's path. */
	private String precedingPath;

	/** The element following the record's path. */
	private String followingPath;

	/**
	 * No-args constructor for reflective use in Jackson.
	 */
	public RecordDefinitionConfiguration() {
		// for reflective construction
	}

	/**
	 * Instantiates a new named record definition configuration.
	 *
	 * @param name
	 *            the name of record
	 * @param kind
	 *            the kind
	 * @param precedingPath
	 *            the preceding path
	 * @param followingPath
	 *            the following path
	 * @param fieldPaths
	 *            the field paths
	 */
	public RecordDefinitionConfiguration(String name, String precedingPath, String followingPath,
			Map<String, String> fieldPaths) {
		this.name = name;
		this.kind = Kind.NAMED;
		this.precedingPath = precedingPath;
		this.followingPath = followingPath;
		this.fieldPaths = fieldPaths;
	}

	/**
	 * Instantiates a new default record definition configuration.
	 *
	 * @param fieldPaths
	 *            the field paths
	 */
	public RecordDefinitionConfiguration(Map<String, String> fieldPaths) {
		this.kind = Kind.DEFAULT;
		this.fieldPaths = fieldPaths;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Sets the kind.
	 *
	 * @param kind
	 *            the new kind
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	/**
	 * Gets the path to the element preceding the record.
	 *
	 * @return the preceding path
	 */
	public String getPrecedingPath() {
		return precedingPath;
	}

	/**
	 * Sets the path to the element preceding the record.
	 *
	 * @param precedingPath
	 *            the new preceding path
	 */
	public void setPrecedingPath(String precedingPath) {
		this.precedingPath = precedingPath;
	}

	/**
	 * Gets the path to the element following the record.
	 *
	 * @return the following path
	 */
	public String getFollowingPath() {
		return followingPath;
	}

	/**
	 * Sets the path to the element following the record.
	 *
	 * @param followingPath
	 *            the new following path
	 */
	public void setFollowingPath(String followingPath) {
		this.followingPath = followingPath;
	}

	/**
	 * Gets the field paths.
	 *
	 * @return the field paths
	 */
	public Map<String, String> getFieldPaths() {
		return fieldPaths;
	}

	/**
	 * Sets the field paths.
	 *
	 * @param fieldPaths
	 *            the field paths
	 */
	public void setFieldPaths(Map<String, String> fieldPaths) {
		this.fieldPaths = fieldPaths;
	}

}
