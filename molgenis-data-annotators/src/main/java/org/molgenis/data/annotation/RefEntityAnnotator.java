package org.molgenis.data.annotation;

import org.molgenis.data.EntityMetaData;

public interface RefEntityAnnotator
{
	/**
	 * Annotators that add a new repository need the source meta data to construct the new repository's meta data.
	 * 
	 * @param sourceEMD
	 *            the meta data of the source repository
	 * @return the meta data of the new repository
	 */
	EntityMetaData getOutputMetaData(EntityMetaData sourceEMD);
}
