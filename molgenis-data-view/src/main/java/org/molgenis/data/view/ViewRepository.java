package org.molgenis.data.view;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ViewRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(ViewRepository.class);
	private final SearchService searchService;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	public ViewRepository(EntityMetaData entityMetaData, DataService dataService, SearchService searchService)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
		this.searchService = requireNonNull(searchService);
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Sets.newHashSet(QUERYABLE);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public EntityMetaData getEntityMetaDataMaster()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
