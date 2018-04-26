package org.molgenis.data.index.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class IndexActionFactory extends AbstractSystemEntityFactory<IndexAction, IndexActionMetaData, String>
{
	IndexActionFactory(IndexActionMetaData indexActionMetaData, EntityPopulator entityPopulator)
	{
		super(IndexAction.class, indexActionMetaData, entityPopulator);
	}
}
