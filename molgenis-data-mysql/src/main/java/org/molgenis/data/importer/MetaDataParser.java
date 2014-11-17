package org.molgenis.data.importer;

import org.molgenis.data.RepositoryCollection;
import org.molgenis.framework.db.EntitiesValidationReport;

public interface MetaDataParser
{
	/**
	 * Parses the metadata of the entities to import.
	 * 
	 * @param source
	 *            {@link RepositoryCollection} containing the data to parse
	 * @return {@link ParsedMetaData}
	 */
	public abstract ParsedMetaData parse(RepositoryCollection source);

	public abstract EntitiesValidationReport validate(RepositoryCollection source);
}