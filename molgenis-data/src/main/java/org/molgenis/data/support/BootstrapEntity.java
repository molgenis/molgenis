package org.molgenis.data.support;

import org.molgenis.data.meta.model.EntityMetaData;

/**
 * Entity used during application bootstrapping. Same as {@link DynamicEntity} but without value validation.
 * During bootstrapping entity meta data might not have initialized yet (e.g. entity meta data is assigned to a package
 * and package has package meta data which is entity meta data).
 */
public class BootstrapEntity extends DynamicEntity
{
	/**
	 * Constructs an entity with the given entity meta data.
	 *
	 * @param entityMeta entity meta
	 */
	public BootstrapEntity(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	protected void validateValueType(String attrName, Object value)
	{
		// no operation
	}
}
