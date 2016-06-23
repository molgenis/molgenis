package org.molgenis.data.reindex.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexActionJobFactory
		extends AbstractSystemEntityFactory<ReindexActionGroup, ReindexActionGroupMetaData, String>
{
	@Autowired
	ReindexActionJobFactory(ReindexActionGroupMetaData reindexActionJobMetaData)
	{
		super(ReindexActionGroup.class, reindexActionJobMetaData);
	}
}
