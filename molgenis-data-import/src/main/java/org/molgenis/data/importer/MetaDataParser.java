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
	 * @param defaultPackage
	 *            , the package where the entities without a package should go
	 * @return {@link ParsedMetaData}
	 */
	public abstract ParsedMetaData parse(RepositoryCollection source, String defaultPackage);

	public abstract EntitiesValidationReport validate(RepositoryCollection source);
}