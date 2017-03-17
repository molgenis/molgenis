package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.gson.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorSort.class)
public abstract class EditorSort
{
	public abstract List<EditorOrder> getOrders();

	public static EditorSort create(ImmutableList<EditorOrder> orders)
	{
		return new AutoValue_EditorSort(orders);
	}
}
