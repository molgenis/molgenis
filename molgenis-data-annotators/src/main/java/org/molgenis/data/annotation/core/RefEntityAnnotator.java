package org.molgenis.data.annotation.core;

import org.molgenis.data.meta.model.EntityMetaData;

public interface RefEntityAnnotator
{
	/**
	 * Annotators that add a new repository need the source meta data to construct the new repository's meta data.
	 * 
	 * @param sourceEMD
	 *            the meta data of the source repository
	 * @return the meta data of the new repository
	 */
	EntityMetaData getTargetEntityMetaData(EntityMetaData sourceEMD);
}
