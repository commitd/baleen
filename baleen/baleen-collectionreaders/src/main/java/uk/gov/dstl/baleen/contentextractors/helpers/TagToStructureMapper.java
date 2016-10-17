package uk.gov.dstl.baleen.contentextractors.helpers;

import java.util.Optional;

import com.tenode.baleen.extraction.Tag;

import uk.gov.dstl.baleen.types.structure.Structure;

public interface TagToStructureMapper {

	/**
	 * Map from a tags to a {@link Structure} annotations.
	 *
	 * @param tag
	 *            the tag to map
	 * @return the mapped structure annotation
	 */
	Optional<Structure> map(final Tag tag);

}