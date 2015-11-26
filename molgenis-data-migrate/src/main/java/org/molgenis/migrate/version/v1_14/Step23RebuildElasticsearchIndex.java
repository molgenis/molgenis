package org.molgenis.migrate.version.v1_14;

import org.molgenis.framework.MolgenisUpgrade;
import org.springframework.stereotype.Component;

/**
 * Rebuilds Elasticsearch index due to changes in mapping for integers fields
 */
@Component
public class Step23RebuildElasticsearchIndex extends MolgenisUpgrade
{
	public Step23RebuildElasticsearchIndex()
	{
		super(22, 23);
	}

	@Override
	public void upgrade()
	{
		// no operation, index is rebuild after migration steps have been executed
	}
}
