package org.molgenis.data.annotation.core;

import org.molgenis.data.meta.model.EntityType;

/**
 * Annotator that annotates variant but instead of adding columns to the variant
 * it creates new "effects" entities with an xref to the variant.
 */
public interface EffectCreatingAnnotator
{
	/**
	 * Annotators that add a new repository need the source meta data to construct the new repository's meta data.
	 *
	 * @param sourceEMD the meta data of the source repository
	 * @return the meta data of the new repository
	 */
	EntityType getTargetEntityType(EntityType sourceEMD);
}
