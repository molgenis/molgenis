package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class IndexSettings
{
	private static final int DEFAULT_NUMBER_OF_SHARDS = 1;
	private static final int DEFAULT_NUMBER_OF_REPLICAS = 0;

	/**
	 * The number of primary shards that an index should have.
	 */
	public abstract int getNumberOfShards();

	/**
	 * The number of replica shards.
	 */
	public abstract int getNumberOfReplicas();

	public static IndexSettings create()
	{
		return builder().build();
	}

	public static IndexSettings create(int newNumberOfShards, int newNumberOfReplicas)
	{
		return builder().setNumberOfShards(newNumberOfShards).setNumberOfReplicas(newNumberOfReplicas).build();
	}

	public static Builder builder()
	{
		return new AutoValue_IndexSettings.Builder().setNumberOfShards(DEFAULT_NUMBER_OF_SHARDS)
													.setNumberOfReplicas(DEFAULT_NUMBER_OF_REPLICAS);
	}

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder setNumberOfShards(int newNumberOfShards);

		public abstract Builder setNumberOfReplicas(int newNumberOfReplicas);

		public abstract IndexSettings build();
	}
}
