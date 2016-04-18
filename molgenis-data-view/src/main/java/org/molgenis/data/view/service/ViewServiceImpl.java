package org.molgenis.data.view.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.JOIN_ATTRIBUTE;
import static org.molgenis.data.view.meta.JoinedAttributeMetaData.MASTER_ATTRIBUTE;
import static org.molgenis.data.view.meta.SlaveEntityMetaData.JOINED_ATTRIBUTES;
import static org.molgenis.data.view.meta.ViewMetaData.MASTER_ENTITY;
import static org.molgenis.data.view.meta.ViewMetaData.NAME;
import static org.molgenis.data.view.meta.ViewMetaData.SLAVE_ENTITIES;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.view.meta.JoinedAttributeMetaData;
import org.molgenis.data.view.meta.SlaveEntityMetaData;
import org.molgenis.data.view.meta.ViewMetaData;
import org.molgenis.data.view.repository.ViewRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

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
		return dataService.findOne(ViewMetaData.ENTITY_NAME, new QueryImpl().eq(NAME, viewName));
	}

	@Override
	public Entity getSlaveEntity(String slaveEntityName)
	{
		Entity slaveEntity = dataService.findOne(SlaveEntityMetaData.ENTITY_NAME,
				new QueryImpl().eq(SlaveEntityMetaData.SLAVE_ENTITY, slaveEntityName));
		return slaveEntity;
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
		Entity newAttributeMapping1 = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		dataService.add(JoinedAttributeMetaData.ENTITY_NAME, newAttributeMapping1);
		Entity newAttributeMapping = newAttributeMapping1;
		Entity slaveEntity = createNewSlaveEntity(slaveEntityName, newAttributeMapping);

		DefaultEntity newViewEntity = new DefaultEntity(viewEntity.getEntityMetaData(), dataService, viewEntity);
		List<Entity> slaveEntities = newArrayList(viewEntity.getEntities(SLAVE_ENTITIES));
		slaveEntities.add(slaveEntity);

		newViewEntity.set(ViewMetaData.SLAVE_ENTITIES, slaveEntities);
		dataService.update(ViewMetaData.ENTITY_NAME, newViewEntity);
	}

	@Override
	public void addNewAttributeMappingToExistingSlave(String slaveEntityName, String masterAttributeId,
			String slaveAttributeId)
	{
		Entity existingSlaveEntity = getSlaveEntity(slaveEntityName);
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
		slaveEntity.set(SlaveEntityMetaData.SLAVE_ENTITY, slaveEntityName);
		slaveEntity.set(SlaveEntityMetaData.JOINED_ATTRIBUTES, newArrayList(newAttributeMapping));
		dataService.add(SlaveEntityMetaData.ENTITY_NAME, slaveEntity);
		return slaveEntity;
	}

}
