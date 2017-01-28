package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorTag.class)
public abstract class EditorTag
{
	abstract String getId();

	public static EditorTag create(String id)
	{
		return new AutoValue_EditorTag(id);
	}
}
