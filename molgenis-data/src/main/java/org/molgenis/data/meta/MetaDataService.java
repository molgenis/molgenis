package org.molgenis.data.meta;

import java.util.Set;

import org.molgenis.data.EntityMetaData;

public interface MetaDataService
{
	Set<EntityMetaData> getAllEntityMetaDataIncludingAbstract();
}
