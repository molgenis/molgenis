package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Sort
{
	public abstract List<SortOrder> getOrders();

	public static Sort create(List<SortOrder> newOrders)
	{
		return builder().setOrders(newOrders).build();
	}

	public static Builder builder()
	{
		return new AutoValue_Sort.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setOrders(List<SortOrder> newOrders);

		public abstract Sort build();
	}
}
