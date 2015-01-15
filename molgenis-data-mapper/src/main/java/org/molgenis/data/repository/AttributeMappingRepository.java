package org.molgenis.data.repository;

import org.molgenis.data.*;
import org.molgenis.data.meta.AttributeMappingMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.*;

/**
 * Helper class around the {@link org.molgenis.data.meta.AttributeMetaDataMetaData} repository. Internal implementation
 * class, use {@link org.molgenis.data.meta.MetaDataServiceImpl} instead.
 */
public class AttributeMappingRepository
{
	public static final AttributeMappingMetaData META_DATA = new AttributeMappingMetaData();

	private final CrudRepository repository;

	public AttributeMappingRepository(ManageableCrudRepositoryCollection collection)
	{
		this.repository = collection.add(META_DATA);
	}
}