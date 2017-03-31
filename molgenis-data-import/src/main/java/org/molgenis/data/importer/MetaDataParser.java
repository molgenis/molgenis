package org.molgenis.data.importer;

import org.molgenis.data.RepositoryCollection;

public interface MetaDataParser
{
	/**
	 * Parses the metadata of the entities to import.
	 *
	 * @param source         {@link RepositoryCollection} containing the data to parse
	 * @param defaultPackage , the package where the entities without a package should go
	 * @return {@link ParsedMetaData}
	 */
	ParsedMetaData parse(RepositoryCollection source,
			String defaultPackage); // FIXME use entity type ids instead of entity type fqns

	/**
	 * Generates a {@link EntitiesValidationReport} by parsing all data from a supplied source
	 *
	 * @param source
	 * @return
	 */
	EntitiesValidationReport validate(RepositoryCollection source);
}