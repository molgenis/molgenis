package org.molgenis.data.elasticsearch.util;

import com.google.auto.value.AutoValue;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import javax.annotation.Nullable;

@AutoValue
public abstract class SearchRequest
{
	@Nullable
	public abstract EntityType getEntityType();

	@Nullable
	public abstract Query<Entity> getQuery();

	@Nullable
	public abstract Attribute getAggregateAttribute1();

	@Nullable
	public abstract Attribute getAggregateAttribute2();

	@Nullable
	public abstract Attribute getAggregateAttributeDistinct();

	public static SearchRequest create(@Nullable EntityType entityType, @Nullable Query<Entity> query,
			@Nullable Attribute aggregateAttribute1, @Nullable Attribute aggregateAttribute2,
			@Nullable Attribute aggregateAttributeDistinct)
	{
		return new AutoValue_SearchRequest(entityType, query, aggregateAttribute1, aggregateAttribute2,
				aggregateAttributeDistinct);
	}
}