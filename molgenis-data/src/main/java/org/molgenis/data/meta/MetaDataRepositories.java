package org.molgenis.data.meta;

import org.molgenis.data.EntityMetaData;

public interface MetaDataRepositories extends MetaDataService
{

	public abstract void registerEntityMetaData(EntityMetaData emd);

}
