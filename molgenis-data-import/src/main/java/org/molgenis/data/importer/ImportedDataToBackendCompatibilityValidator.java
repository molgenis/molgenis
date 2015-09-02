package org.molgenis.data.importer;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableCollection;

public class ImportedDataToBackendCompatibilityValidator
{
	@Autowired
	DataService dataService;

	public ImportedDataToBackendCompatibilityValidator(DataService dataService)
	{
		this.dataService = dataService;
	}

	public void validate(ImmutableCollection<EntityMetaData> immutableCollection)
	{
		immutableCollection.forEach(entity -> validateEntity(entity));
	}

	private void validateEntity(EntityMetaData entity)
	{
		if (dataService.hasRepository(entity.getName()) || dataService.hasRepository(entity.getName().toLowerCase()))
		{
			throw new MolgenisDataException("Entity with name [" + entity.getName()
					+ "] already exists. Casing is ignored!");
		}
	}
}
