package org.molgenis.data.elasticsearch.client.model;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SearchHits
{
	public abstract long getTotalHits();

	public abstract List<SearchHit> getHits();

	public static SearchHits create(long newTotalHits, List<SearchHit> newHits)
	{
		return builder().setTotalHits(newTotalHits).setHits(newHits).build();
	}

	public static Builder builder()
	{
		return new AutoValue_SearchHits.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setTotalHits(long newTotalHits);

		public abstract Builder setHits(List<SearchHit> newHits);

		public abstract SearchHits build();
	}
}
