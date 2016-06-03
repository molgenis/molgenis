package org.molgenis.data.reindex.meta;

import org.molgenis.data.AbstractEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexActionJobFactory extends AbstractEntityFactory<ReindexActionJob, ReindexActionJobMetaData, String>
{
	@Autowired
	ReindexActionJobFactory(ReindexActionJobMetaData reindexActionJobMetaData)
	{
		super(ReindexActionJob.class, reindexActionJobMetaData, String.class);
	}
}
