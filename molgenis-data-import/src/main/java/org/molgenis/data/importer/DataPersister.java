package org.molgenis.data.importer;

import org.molgenis.data.DatabaseAction;

/**
 * Persists data and metadata provided by a {@link DataProvider} using a {@link DatabaseAction}.
 */
public interface DataPersister
{
	/**
	 * Data persist mode
	 */
	enum DataMode
	{
		ADD, UPDATE, UPSERT
	}

	/**
	 * Metadata persist mode
	 */
	enum MetadataMode
	{
		ADD, UPDATE, UPSERT, NONE
	}

	/**
	 * Persists data and metadata
	 *
	 * @param dataProvider data and metadata provider
	 * @param metadataMode metadata persists mode
	 * @param dataMode     data persist mode
	 * @return persist result containing e.g. number of persisted data elements
	 */
	PersistResult persist(DataProvider dataProvider, MetadataMode metadataMode, DataMode dataMode);
}
