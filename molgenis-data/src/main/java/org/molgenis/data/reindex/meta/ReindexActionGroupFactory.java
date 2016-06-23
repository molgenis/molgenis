package org.molgenis.data.reindex.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexActionGroupFactory
		extends AbstractSystemEntityFactory<ReindexActionGroup, ReindexActionGroupMetaData, String>
{
	@Autowired
	ReindexActionGroupFactory(ReindexActionGroupMetaData reindexActionJobMetaData)
	{
		super(ReindexActionGroup.class, reindexActionJobMetaData);
	}
}
