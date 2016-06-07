package org.molgenis.data.reindex.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexActionFactory extends AbstractSystemEntityFactory<ReindexAction, ReindexActionMetaData, String>
{
	@Autowired
	ReindexActionFactory(ReindexActionMetaData reindexActionMetaData)
	{
		super(ReindexAction.class, reindexActionMetaData, String.class);
	}
}
