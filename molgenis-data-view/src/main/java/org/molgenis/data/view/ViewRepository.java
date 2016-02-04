package org.molgenis.data.view;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.RepositoryCapability.QUERYABLE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ViewRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(ViewRepository.class);
	private final SearchService searchService;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	public static final int BATCH_SIZE = 1000;

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
		DefaultEntityMetaData entityMetaDataView = new DefaultEntityMetaData(entityMetaData);
		
		// Add master compound
		String entityMasterName = getMasterEntityName();
		DefaultAttributeMetaData masterCompoundAttribute = new DefaultAttributeMetaData(entityMasterName, FieldTypeEnum.COMPOUND); 
		masterCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(entityMasterName).getAttributes()); 
		entityMetaDataView.addAttributeMetaData(masterCompoundAttribute,AttributeRole.ROLE_LOOKUP);
		  
		// Add slave compounds
		Set<String> entitySlaveNames = dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName()).findAll().map(e -> {
					return e.getString(EntityViewMetaData.JOIN_ENTITY);
				}).collect(Collectors.toSet());
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
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		// String masterEntityName = getMasterEntityName();
		
		//Master attributes to join on
		List<String> joinMasterMatrix = new ArrayList<String>();
		
		//join attributes to join on
		Map<String, List<String>> joinSlaveMatrix = new LinkedHashMap<String, List<String>>();
				
		dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName())
				.findAll().forEach(e -> {
							joinMasterMatrix.add(e.getString(EntityViewMetaData.MASTER_ATTR));
							if (!joinSlaveMatrix.containsKey(e.getString(EntityViewMetaData.JOIN_ENTITY))) joinSlaveMatrix
									.put(e.getString(EntityViewMetaData.JOIN_ENTITY), new ArrayList<String>());
							joinSlaveMatrix.get(e.getString(EntityViewMetaData.JOIN_ENTITY)).add(
									e.getString(EntityViewMetaData.JOIN_ATTR));
				});
		
		Stream<Entity> allMasterEntityEntities = dataService.getRepository(getMasterEntityName()).findAll(
				new QueryImpl());
				
		EntityMetaData emd = getEntityMetaData();

		return StreamSupport.stream(allMasterEntityEntities.spliterator(), false).map(e -> {
			return getViewEntity(e, emd, joinMasterMatrix, joinSlaveMatrix);
		});
	}

	private Entity getViewEntity(Entity entity, EntityMetaData metaData, List<String> joinMasterMatrix,
			Map<String, List<String>> joinSlaveMatrix)
	{
		MapEntity me = new MapEntity(metaData);
		me.set(entity);
		
		for(Entry<String, List<String>> entry :joinSlaveMatrix.entrySet()){
			Query q = new QueryImpl();
			for(int i=0; i < joinMasterMatrix.size(); i++){
				if(i != 0) q.and();
				q.eq(entry.getValue().get(i), entity.get(joinMasterMatrix.get(i)));
			}
			me.set(dataService.findOne(entry.getKey(), q)); // TODO can be more then one what to do
		}
		return entity;
	}

	private String getMasterEntityName()
	{
		return dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName()).findOne()
				.getString(EntityViewMetaData.MASTER_ENTITY);

	}

	@Override
	public void create()
	{
		// Skip this the. The view is virtual.
	}
}
