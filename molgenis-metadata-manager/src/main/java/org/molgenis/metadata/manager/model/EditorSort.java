package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import org.molgenis.util.AutoGson;

import java.util.List;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorSort.class)
@SuppressWarnings("squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorSort
{
	public abstract List<EditorOrder> getOrders();

	public static EditorSort create(ImmutableList<EditorOrder> orders)
	{
		return new AutoValue_EditorSort(orders);
	}
}
