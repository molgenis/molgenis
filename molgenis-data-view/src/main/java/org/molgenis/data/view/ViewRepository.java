package org.molgenis.data.view;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
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
		String entityMasterName = dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName()).findOne().getString(EntityViewMetaData.MASTER_ENTITY);
		
		List<String> entitySlaveNames = dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName()).findAll().map(e -> {
					return e.getString(EntityViewMetaData.MASTER_ENTITY);
				}).collect(Collectors.toList());
		
		DefaultEntityMetaData entityMetaDataView = new DefaultEntityMetaData(entityMetaData);
		
		// Add master compound
		DefaultAttributeMetaData masterCompoundAttribute = new DefaultAttributeMetaData(entityMasterName, FieldTypeEnum.COMPOUND); 
		masterCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(entityMasterName).getAttributes()); 
		entityMetaDataView.addAttributeMetaData(masterCompoundAttribute,AttributeRole.ROLE_LOOKUP);
		  
		// Add slave compounds
		entitySlaveNames.spliterator().forEachRemaining(e -> {
			DefaultAttributeMetaData slaveCompoundAttribute = new DefaultAttributeMetaData(e, FieldTypeEnum.COMPOUND); 
			slaveCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(e).getAttributes()); 
			entityMetaDataView.addAttributeMetaData(slaveCompoundAttribute, AttributeRole.ROLE_LOOKUP);
		});

		return entityMetaDataView;
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
