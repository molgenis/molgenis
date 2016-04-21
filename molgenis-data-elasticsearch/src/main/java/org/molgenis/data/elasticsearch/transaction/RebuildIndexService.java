package org.molgenis.data.elasticsearch.transaction;

import org.molgenis.data.transaction.MolgenisTransactionListener;

public interface RebuildIndexService extends MolgenisTransactionListener
{
	void rebuildIndex(String transactionId);
}
