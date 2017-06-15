package org.molgenis.data.elasticsearch.client.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SearchHit
{
	public abstract String getId();

	public abstract String getIndex();

	public static SearchHit create(String newId, String newIndex)
	{
		return builder().setId(newId).setIndex(newIndex).build();
	}

	public static Builder builder()
	{
		return new AutoValue_SearchHit.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setId(String newId);

		public abstract Builder setIndex(String newIndex);

		public abstract SearchHit build();
	}
}
