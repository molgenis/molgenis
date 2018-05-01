package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityTypeParent.class)
public abstract class EditorEntityTypeParent
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	public abstract List<EditorAttributeIdentifier> getAttributes();

	@Nullable
	public abstract EditorEntityTypeParent getParent();

	public static EditorEntityTypeParent create(String id, @Nullable String label,
			List<EditorAttributeIdentifier> attributes, @Nullable EditorEntityTypeParent parent)
	{
		return new AutoValue_EditorEntityTypeParent(id, label, attributes, parent);
	}
}
