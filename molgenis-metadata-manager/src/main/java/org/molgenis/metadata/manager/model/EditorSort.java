package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.core.gson.AutoGson;

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
