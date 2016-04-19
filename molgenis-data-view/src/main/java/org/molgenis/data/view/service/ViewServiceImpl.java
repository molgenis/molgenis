package org.molgenis.data.view.service;

import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.JOIN_ATTRIBUTE;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.MASTER_ATTRIBUTE;
import static org.molgenis.data.view.meta.SlaveEntityMetaData.JOINED_ATTRIBUTES;
import static org.molgenis.data.view.meta.SlaveEntityMetaData.SLAVE_ENTITY;
import static org.molgenis.data.view.meta.ViewMetaData.MASTER_ENTITY;
import static org.molgenis.data.view.meta.ViewMetaData.NAME;
import static org.molgenis.data.view.meta.ViewMetaData.SLAVE_ENTITIES;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;

import org.elasticsearch.cluster.metadata.MetaDataService;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.JoinedAttributeMetaData;
import org.molgenis.data.view.meta.SlaveEntityMetaData;
import org.molgenis.data.view.meta.ViewMetaData;
import org.molgenis.data.view.repository.ViewRepository;
import org.molgenis.data.view.repository.ViewRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;

@Component
public class ViewServiceImpl implements ViewService, ApplicationListener<ContextRefreshedEvent>
{
	private static final Logger LOG = LoggerFactory.getLogger(ViewServiceImpl.class);

	@Autowired
	DataServiceImpl dataService;

	@Autowired
	ViewRepositoryCollection viewRepositoryCollection;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		LOG.info("Registering VIEW repositories with the DataService...");
		runAsSystem(this::registerViews);
	}

	/**
	 * Bootstrapping.
	 * 
	 * Adds {@link ViewRepository}s to the {@link ViewRepositoryCollection} and registers them with the
	 * {@link DataService}.
	 * 
	 * The {@link DataServiceImpl#addRepository(Repository)} method used is not present in the {@link DataService}
	 * interface. It's supposed to only be used by the {@link MetaDataService}. But the views are special cause their
	 * {@link EntityMetaData} is dynamic. It changes if its master or one of its slave {@link EntityMetaData} changes.
	 * 
	 * There's no way yet to listen for such updates or to have computed {@link EntityMetaData} so we bypass the
	 * {@link MetaDataService} altogether.
	 */
	private void registerViews()
	{
		dataService.findAll(ViewMetaData.ENTITY_NAME).map(viewEntity -> viewEntity.getString(NAME))
				.forEach(this::registerRepository);
	}

	private void registerRepository(String viewName)
	{
		Repository viewRepository = viewRepositoryCollection.addViewRepository(viewName);
		dataService.addRepository(viewRepository);
	}

	@Override
	public Entity getViewEntity(String viewName)
	{
		Query query = new QueryImpl().eq(NAME, viewName);
		Fetch fetch = query.fetch();
		fetch.field(ViewMetaData.IDENTIFIER);
		fetch.field(ViewMetaData.MASTER_ENTITY);
		fetch.field(ViewMetaData.NAME);
		Fetch slaveFetch = new Fetch();
		slaveFetch.field(SlaveEntityMetaData.IDENTIFIER);
		slaveFetch.field(SlaveEntityMetaData.SLAVE_ENTITY);
		slaveFetch.field(SlaveEntityMetaData.JOINED_ATTRIBUTES);
		fetch.field(ViewMetaData.SLAVE_ENTITIES, slaveFetch);
		return dataService.findOne(ViewMetaData.ENTITY_NAME, query);
	}

	@Override
	public Optional<Entity> getSlaveEntity(String viewName, String slaveEntityName)
	{
		Entity viewEntity = getViewEntity(viewName);
		Iterable<Entity> slaveEntities = viewEntity.getEntities(SLAVE_ENTITIES);
		return tryFind(slaveEntities, e -> e.getString(SLAVE_ENTITY).equals(slaveEntityName));
	}

	@Override
	public void createNewView(String viewName, String masterEntityName, String slaveEntityName,
			String masterAttributeId, String slaveAttributeId)
	{
		Entity newAttributeMapping1 = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		dataService.add(JoinedAttributeMetaData.ENTITY_NAME, newAttributeMapping1);
		Entity newAttributeMapping = newAttributeMapping1;
		Entity slaveEntity = createNewSlaveEntity(slaveEntityName, newAttributeMapping);

		Entity newViewEntity = new MapEntity(new ViewMetaData());
		newViewEntity.set(NAME, viewName);
		newViewEntity.set(MASTER_ENTITY, masterEntityName);
		newViewEntity.set(SLAVE_ENTITIES, newArrayList(slaveEntity));
		dataService.add(ViewMetaData.ENTITY_NAME, newViewEntity);

		registerRepository(viewName);
	}

	@Override
	public void addNewSlaveEntityToExistingView(Entity viewEntity, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId)
	{
		Entity newAttributeMapping = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		dataService.add(JoinedAttributeMetaData.ENTITY_NAME, newAttributeMapping);
		Entity slaveEntity = createNewSlaveEntity(slaveEntityName, newAttributeMapping);
		List<Entity> slaveEntities = newArrayList(viewEntity.getEntities(SLAVE_ENTITIES));
		slaveEntities.add(slaveEntity);
		viewEntity.set(ViewMetaData.SLAVE_ENTITIES, slaveEntities);
		dataService.update(ViewMetaData.ENTITY_NAME, viewEntity);
	}

	@Override
	public void addNewAttributeMappingToExistingSlave(String viewName, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId)
	{
		Entity existingSlaveEntity = getSlaveEntity(viewName, slaveEntityName).get();
		Entity newAttributeMapping = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		// TODO: why create a new one??
		DefaultEntity newSlaveEntity = new DefaultEntity(new SlaveEntityMetaData(), dataService, existingSlaveEntity);
		List<Entity> attributeMappingList = newArrayList(
				existingSlaveEntity.getEntities(SlaveEntityMetaData.JOINED_ATTRIBUTES));
		if (attributeMappingList.stream().anyMatch(existingMapping -> matches(existingMapping, newAttributeMapping)))
		{
			throw new IllegalArgumentException("Mapping already exists!");
		}
		attributeMappingList.add(newAttributeMapping);
		dataService.add(JoinedAttributeMetaData.ENTITY_NAME, newAttributeMapping);
		newSlaveEntity.set(JOINED_ATTRIBUTES, attributeMappingList);
		dataService.update(SlaveEntityMetaData.ENTITY_NAME, newSlaveEntity);
	}

	private boolean matches(Entity attributeMappingEntity, Entity newAttributeMappingEntity)
	{
		return attributeMappingEntity.getString(MASTER_ATTRIBUTE)
				.equals(newAttributeMappingEntity.getString(MASTER_ATTRIBUTE))
				&& attributeMappingEntity.getString(JOIN_ATTRIBUTE)
						.equals(newAttributeMappingEntity.getString(JOIN_ATTRIBUTE));
	}

	private Entity createNewAttributeMappingEntity(String masterAttributeId, String slaveAttributeId)
	{
		Entity masterAttribute = dataService.findOne(AttributeMetaDataMetaData.ENTITY_NAME, masterAttributeId);
		Entity slaveAttribute = dataService.findOne(AttributeMetaDataMetaData.ENTITY_NAME, slaveAttributeId);
		Entity newAttributeMapping = new MapEntity(new JoinedAttributeMetaData());
		newAttributeMapping.set(JoinedAttributeMetaData.MASTER_ATTRIBUTE,
				masterAttribute.get(AttributeMetaDataMetaData.NAME));
		newAttributeMapping.set(JoinedAttributeMetaData.JOIN_ATTRIBUTE,
				slaveAttribute.get(AttributeMetaDataMetaData.NAME));
		return newAttributeMapping;
	}

	private Entity createNewSlaveEntity(String slaveEntityName, Entity newAttributeMapping)
	{
		Entity slaveEntity = new MapEntity(new SlaveEntityMetaData());
		slaveEntity.set(SLAVE_ENTITY, slaveEntityName);
		slaveEntity.set(JOINED_ATTRIBUTES, newArrayList(newAttributeMapping));
		dataService.add(SlaveEntityMetaData.ENTITY_NAME, slaveEntity);
		return slaveEntity;
	}

	@Override
	public void deleteView(String viewName)
	{
		dataService.removeRepository(viewName);
		Entity view = getViewEntity(viewName);
		
		// manual fetch :)
		for (Entity slave : view.getEntities(SLAVE_ENTITIES))
		{
			slave.getEntities(JOINED_ATTRIBUTES);
		}
		
		dataService.delete(ViewMetaData.ENTITY_NAME, view);
		try
		{
			for (Entity slave : view.getEntities(SLAVE_ENTITIES))
			{
				dataService.delete(SlaveEntityMetaData.ENTITY_NAME, slave);
				for (Entity joinedAttribute : slave.getEntities(JOINED_ATTRIBUTES))
				{
					dataService.delete(JoinedAttributeMetaData.ENTITY_NAME, joinedAttribute);
				}
			}
		}
		catch (Exception ex)
		{
			LOG.warn("Failed to clean up the view details", ex);
		}
		
	}

}
