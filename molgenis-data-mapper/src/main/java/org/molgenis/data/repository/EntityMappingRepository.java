package org.molgenis.data.repository;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.meta.EntityMappingMetaData;

/**
 * Helper class around the {@link org.molgenis.data.meta.AttributeMetaDataMetaData} repository. Internal implementation
 * class, use {@link org.molgenis.data.meta.MetaDataServiceImpl} instead.
 */
public class EntityMappingRepository
{
	public static final EntityMappingMetaData META_DATA = new EntityMappingMetaData();

	private final CrudRepository repository;

	public EntityMappingRepository(ManageableCrudRepositoryCollection collection)
	{
		this.repository = collection.add(META_DATA);
	}
}