package uk.gov.dstl.baleen.uima.utils;

import uk.gov.dstl.baleen.types.structure.Structure;

/**
 * Internally used class to represent part of a selector (eg between the &gt;
 * operator). Not intended for public use.
 */
class SelectorPart {

	/** The Baleen structural type. */
	private final Class<Structure> type;

	/** The psuedo selector clause, if present. */
	private final String psuedoSelectorClause;

	/**
	 * Instantiates a new selector part.
	 *
	 * @param structureType
	 *            the structure type
	 */
	SelectorPart(Class<Structure> structureType) {
		this(structureType, null);
	}

	/**
	 * Instantiates a new selector part.
	 *
	 * @param structureType
	 *            the structure type
	 * @param psuedoSelector
	 *            the psuedo selector
	 */
	SelectorPart(Class<Structure> structureType, String psuedoSelector) {
		this.type = structureType;
		this.psuedoSelectorClause = psuedoSelector;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Class<Structure> getType() {
		return type;
	}

	/**
	 * Gets the psuedo selector clause.
	 *
	 * @return the psuedo selector clause or null of none present.
	 */
	public String getPsuedoSelectorClause() {
		return psuedoSelectorClause;
	}

}