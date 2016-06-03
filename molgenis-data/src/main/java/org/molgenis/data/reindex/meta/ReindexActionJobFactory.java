package org.molgenis.data.reindex.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexActionJobFactory extends AbstractEntityFactory<ReindexAction, ReindexActionMetaData, String>
{
	@Autowired
	IdCardBiobankFactory(ReindexActionMetaData reindexActionMetaData)
	{
		super(ReindexAction.class, reindexActionMetaData, Integer.class);
	}
}
