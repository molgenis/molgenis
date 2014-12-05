package org.molgenis.ontology.beans;

import javax.annotation.Nullable;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;

import com.google.common.base.Function;

public class OntologyTermTransformer implements Function<Entity, Entity>
{
	private final EntityMetaData entityMetaData;
	private final SearchService searchService;

	public OntologyTermTransformer(EntityMetaData entityMetaData, SearchService searchService)
	{
		this.entityMetaData = entityMetaData;
		this.searchService = searchService;
	}

	@Override
	@Nullable
	public Entity apply(@Nullable
	Entity input)
	{
		return new OntologyTermEntity(input, entityMetaData, searchService);
	}
}