package org.molgenis.model.registry.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author sido
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_ModelRegistryPackage.class)
public abstract class ModelRegistryPackage
{
	public static ModelRegistryPackage create(String name, String label, String description, String matchDescription,
			List<ModelRegistryEntity> entitiesInPackage, List<ModelRegistryTag> tags)
	{
		return new AutoValue_ModelRegistryPackage(name, label, description, matchDescription, entitiesInPackage, tags);
	}

	@SuppressWarnings("unused")
	public abstract String getName();

	@Nullable
	public abstract String getLabel();

	@SuppressWarnings("unused")
	@Nullable
	public abstract String getDescription();

	@SuppressWarnings("unused")
	@Nullable
	public abstract String getMatchDescription();

	@SuppressWarnings("unused")
	@Nullable
	public abstract List<ModelRegistryEntity> getEntities();

	@SuppressWarnings("unused")
	@Nullable
	public abstract Iterable<ModelRegistryTag> getTags();

}
