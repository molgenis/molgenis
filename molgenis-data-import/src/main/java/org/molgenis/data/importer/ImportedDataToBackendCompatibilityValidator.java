package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class serves as an early validator. It can check if an entity is valid before we process parsed data into
 * repositories.
 *
 */
public class ImportedDataToBackendCompatibilityValidator
{
	@Autowired
	DataService dataService;

	public ImportedDataToBackendCompatibilityValidator(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void validate(Iterable<EntityMetaData> immutableCollection)
	{
		immutableCollection.forEach(entity -> checkIfEntityExists(entity));
	}

	private void checkIfEntityExists(EntityMetaData entity)
	{
		if (dataService.hasRepository(entity.getName()) || dataService.hasRepository(entity.getName().toLowerCase()))
		{
			throw new MolgenisDataException("Entity with name [" + entity.getName()
					+ "] already exists. Casing is ignored!");
		}
	}
}
