package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeParent.class)
public abstract class EditorEntityTypeParent
{
	abstract String getId();

	@Nullable
	abstract String getLabel();

	abstract ImmutableList<EditorAttributeIdentifier> getAttributes();

	@Nullable
	abstract EditorEntityTypeParent getParent();

	public static EditorEntityTypeParent create(String id, @Nullable String label,
			ImmutableList<EditorAttributeIdentifier> attributes, @Nullable EditorEntityTypeParent parent)
	{
		return new AutoValue_EditorEntityTypeParent(id, label, attributes, parent);
	}
}
