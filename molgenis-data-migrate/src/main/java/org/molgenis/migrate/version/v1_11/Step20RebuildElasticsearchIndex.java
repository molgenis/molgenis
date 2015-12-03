package org.molgenis.migrate.version.v1_11;

import org.molgenis.framework.MolgenisUpgrade;
import org.springframework.stereotype.Component;

/**
 * Rebuilds Elasticsearch index due to changes in which entities are (not) stored in the Elasticsearch index
 */
@Component
public class Step20RebuildElasticsearchIndex extends MolgenisUpgrade
{
	public Step20RebuildElasticsearchIndex()
	{
		super(19, 20);
	}

	@Override
	public void upgrade()
	{
		// no operation, index is rebuild after migration steps have been executed
	}
}
