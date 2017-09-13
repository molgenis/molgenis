package org.molgenis.data.index.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class IndexActionGroupFactory
		extends AbstractSystemEntityFactory<IndexActionGroup, IndexActionGroupMetaData, String>
{
	IndexActionGroupFactory(IndexActionGroupMetaData indexActionGroupMetaData, EntityPopulator entityPopulator)
	{
		super(IndexActionGroup.class, indexActionGroupMetaData, entityPopulator);
	}
}
