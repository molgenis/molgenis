package org.molgenis.data.importer;

import org.molgenis.data.RepositoryCollection;

public interface MetaDataParser
{
	/**
	 * Parses the metadata of the entities to import.
	 *
	 * @param source    {@link RepositoryCollection} containing the data to parse
	 * @param packageId , the package where the entities should go. Default if none was supplied
	 * @return {@link ParsedMetaData}
	 */
	ParsedMetaData parse(RepositoryCollection source, String packageId);

	/**
	 * Generates a {@link EntitiesValidationReport} by parsing all data from a supplied source
	 *
	 * @param source
	 * @return
	 */
	EntitiesValidationReport validate(RepositoryCollection source);
}