package org.molgenis.data.repository;

import org.molgenis.data.*;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;

import static org.molgenis.data.meta.AttributeMetaDataMetaData.*;

/**
 * Helper class around the {@link org.molgenis.data.meta.AttributeMetaDataMetaData} repository. Internal implementation
 * class, use {@link org.molgenis.data.meta.MetaDataServiceImpl} instead.
 */
public class AttributeMappingRepository
{
	public static final EntityMappingMetaData META_DATA = new EntityMappingMetaData();

	private final CrudRepository repository;

	public AttributeMappingRepository(ManageableCrudRepositoryCollection collection)
	{
		this.repository = collection.add(META_DATA);
		fillAllEntityAttributes();
	}

	public void fillAllEntityAttributes()
	{

	}

	private void toEntityMappingEntity(Entity entity, AttributeMetaData att, AttributeMetaData parentCompoundAtt)
	{
		Entity attributeMetaDataEntity = new MapEntity();
	}

	private DefaultAttributeMetaData toAttributeMetaData(Entity entity)
	{
		DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(entity.getString(NAME));
		return attributeMetaData;
	}
}