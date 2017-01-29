package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorOrder.class)
public abstract class EditorOrder
{
	public abstract String getAttributeName();

	@Nullable
	public abstract String getDirection();

	public static EditorOrder create(String attributeName, @Nullable String direction)
	{
		return new AutoValue_EditorOrder(attributeName, direction);
	}
}
