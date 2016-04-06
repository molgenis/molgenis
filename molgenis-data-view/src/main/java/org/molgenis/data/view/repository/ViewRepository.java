package org.molgenis.data.view.repository;

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
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.EntityMetaData.AttributeRole;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.EntityViewMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class ViewRepository extends AbstractRepository
{
	private static final Logger LOG = LoggerFactory.getLogger(ViewRepository.class);
	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	public static final int BATCH_SIZE = 1000;

	public ViewRepository(EntityMetaData entityMetaData, DataService dataService)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
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
		DefaultAttributeMetaData masterCompoundAttribute = new DefaultAttributeMetaData("_" + entityMasterName,
				FieldTypeEnum.COMPOUND);
		masterCompoundAttribute.setAttributesMetaData(dataService.getEntityMetaData(entityMasterName).getAttributes()); 
		entityMetaDataView.addAttributeMetaData(masterCompoundAttribute, AttributeRole.ROLE_LOOKUP);
		  
		// Add slave compounds
		dataService
				.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName())
				.findAll()
				.map(e -> {
					return e.getString(EntityViewMetaData.JOIN_ENTITY);
				})
				.collect(Collectors.toSet())
				.stream()
				.forEach(
						e -> {
							DefaultAttributeMetaData slaveCompoundAttribute = new DefaultAttributeMetaData(e,
									FieldTypeEnum.COMPOUND);
							slaveCompoundAttribute.setAttributesMetaData(StreamSupport
											.stream(dataService.getEntityMetaData(e).getAtomicAttributes()
													.spliterator(), false)
									.map(f -> {
												String prefixedAttributeName = prefixSlaveEntityAttributeName(e,
														f.getName());
												return new DefaultAttributeMetaData(prefixedAttributeName,
														prefixedAttributeName, f);
									}).collect(Collectors.toList()));
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
	public long count(Query q)
	{
		return dataService.count(getMasterEntityName(), q);
	}

	@Override
	public Stream<Entity> findAll(Query q)
	{
		//Master attributes to join on
		List<String> masterJoinAttributes = new ArrayList<String>();
		
		//join attributes to join on
		Map<String, List<String>> slaveJoinAttributes = new LinkedHashMap<String, List<String>>();
				
		dataService.query(EntityViewMetaData.ENTITY_NAME)
				.eq(EntityViewMetaData.VIEW_NAME, entityMetaData.getName())
				.findAll().forEach(e -> {
							if (!masterJoinAttributes.contains(e.getString(EntityViewMetaData.MASTER_ATTR))) masterJoinAttributes
									.add(e.getString(EntityViewMetaData.MASTER_ATTR));
							if (!slaveJoinAttributes.containsKey(e.getString(EntityViewMetaData.JOIN_ENTITY))) slaveJoinAttributes
									.put(e.getString(EntityViewMetaData.JOIN_ENTITY), new ArrayList<String>());
							slaveJoinAttributes.get(e.getString(EntityViewMetaData.JOIN_ENTITY)).add(
									e.getString(EntityViewMetaData.JOIN_ATTR));
				});
		
		Stream<Entity> allMasterEntityEntities = dataService.getRepository(getMasterEntityName()).findAll(q);
				
		EntityMetaData emd = getEntityMetaData();

		return StreamSupport.stream(allMasterEntityEntities.spliterator(), false).map(e -> {
			return getViewEntity(e, emd, masterJoinAttributes, slaveJoinAttributes);
		});
	}

	@Override
	public Entity findOne(Object id, Fetch fetch)
	{
		if (id == null) return null;
		return findOne(new QueryImpl().eq(getEntityMetaData().getIdAttribute().getName(), id).fetch(fetch));
	}

	private Entity getViewEntity(Entity masterEntity, EntityMetaData viewMetaData, List<String> joinMasterMatrix,
			Map<String, List<String>> joinSlaveMatrix)
	{
		DefaultEntity me = new DefaultEntity(viewMetaData, dataService);
		me.setOnlyAttributesWithSameMetadata(masterEntity);
		
		for (Entry<String, List<String>> entry : joinSlaveMatrix.entrySet())
		{
			Query q = new QueryImpl();
			for (int i = 0; i < joinMasterMatrix.size(); i++)
			{
				if (i != 0) q.and();
				q.eq(entry.getValue().get(i), masterEntity.get(joinMasterMatrix.get(i)));
			}

			List<Entity> slaveEntities = dataService.findAll(entry.getKey(), q).collect(Collectors.toList());

			if (slaveEntities.size() > 1)
			{
				throw new MolgenisDataException("For the query: " + q + " in entity name: " + entry.getKey()
						+ " where " + slaveEntities.size()
						+ " results found. The VIEW backend supports only one or zero result");
			}

			if (slaveEntities.size() == 1)
			{
				for (String attributeName : slaveEntities.get(0).getAttributeNames())
				{
					me.set(prefixSlaveEntityAttributeName(entry.getKey(), attributeName),
							slaveEntities.get(0).get(attributeName));
				}
			}
		}
		return me;
	}

	private String prefixSlaveEntityAttributeName(String entityName, String attributeName){
		return entityName + "__" + attributeName;
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
		/**
		 * Skip this method. The view is dynamic and have not real physical copy.
		 */
	}

	@Override
	public Iterable<AttributeMetaData> getQueryableAttributes()
	{
		return dataService.getMeta().getEntityMetaData(getMasterEntityName()).getAtomicAttributes();
	}
}
