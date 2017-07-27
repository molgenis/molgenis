package org.molgenis.model.registry.model;

import com.google.auto.value.AutoValue;

/**
 * @author sido
 */
@AutoValue
public abstract class ModelRegistryEntity
{

	public static ModelRegistryEntity create(String name, String label, boolean abstr)
	{
		return new AutoValue_ModelRegistryEntity(name, label, abstr);
	}

	public abstract String getName();

	@SuppressWarnings("unused")
	public abstract String getLabel();

	public abstract boolean isAbstract();

}
