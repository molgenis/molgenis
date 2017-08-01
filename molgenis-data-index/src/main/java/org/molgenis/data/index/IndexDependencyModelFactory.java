package org.molgenis.data.index;

import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.AttributeMetadata.REF_ENTITY_TYPE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.*;

@Component
public class IndexDependencyModelFactory
{
	private static final int ENTITY_FETCH_PAGE_SIZE = 1000;

	/**
	 * The fetch to use when retrieving the {@link EntityType}s fed to this DependencyModel.
	 */
	private static final Fetch ENTITY_TYPE_FETCH = new Fetch().field(ID)
															  .field(IS_ABSTRACT)
															  .field(INDEXING_DEPTH)
															  .field(EXTENDS, new Fetch().field(ID))
															  .field(ATTRIBUTES, new Fetch().field(REF_ENTITY_TYPE,
																	  new Fetch().field(ID)));
	private final DataService dataService;

	public IndexDependencyModelFactory(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public IndexDependencyModel getDependencyModel()
	{
		return new IndexDependencyModel(getEntityTypes());
	}

	/**
	 * Retrieves all {@link EntityType}s.
	 * Queryies in pages of size ENTITY_FETCH_PAGE_SIZE so that results can be cached.
	 * Uses ENTITY_TYPE_FETCH which specifies all fields needed to figure out the dependencies.
	 *
	 * @return List containing all {@link EntityType}s.
	 */
	private List<EntityType> getEntityTypes()
	{
		QueryImpl<EntityType> query = new QueryImpl<>();
		query.setPageSize(ENTITY_FETCH_PAGE_SIZE);
		query.setFetch(ENTITY_TYPE_FETCH);

		List<EntityType> result = newArrayList();
		for (int pageNum = 0; result.size() == pageNum * ENTITY_FETCH_PAGE_SIZE; pageNum++)
		{
			query.offset(pageNum * ENTITY_FETCH_PAGE_SIZE);
			dataService.findAll(ENTITY_TYPE_META_DATA, query, EntityType.class).forEach(result::add);
		}
		return result;
	}
}
