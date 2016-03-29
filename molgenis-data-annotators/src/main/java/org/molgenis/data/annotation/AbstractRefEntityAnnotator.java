package org.molgenis.data.annotation;

import org.molgenis.data.EntityMetaData;

public abstract class AbstractRefEntityAnnotator extends AbstractRepositoryAnnotator
{
	/**
	 * Annotators that add a new repository need the source meta data to construct the new repository's meta data.
	 * 
	 * @param sourceEMD
	 *            the meta data of the source repository
	 * @return the meta data of the new repository
	 */
	public abstract EntityMetaData getOutputMetaData(EntityMetaData sourceEMD);
}
