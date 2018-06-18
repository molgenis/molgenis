package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttributeIdentifier.class)
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorAttributeIdentifier
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	@Nullable
	public abstract EditorEntityTypeIdentifier getEntity();

	public static EditorAttributeIdentifier create(String id, @Nullable String label)
	{
		return new AutoValue_EditorAttributeIdentifier(id, label, null);
	}

	public static EditorAttributeIdentifier create(String id, @Nullable String label, @Nullable EditorEntityTypeIdentifier entity)
	{
		return new AutoValue_EditorAttributeIdentifier(id, label, entity);
	}
}
