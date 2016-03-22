package org.molgenis.data.annotation;

import org.molgenis.data.EntityMetaData;

public abstract class AbstractExternalRepositoryAnnotator extends AbstractRepositoryAnnotator
{
	public abstract EntityMetaData getOutputMetaData(EntityMetaData sourceEMD);
}
