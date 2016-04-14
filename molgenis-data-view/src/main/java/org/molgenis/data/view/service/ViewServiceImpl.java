package org.molgenis.data.view.service;

import static com.google.common.collect.Lists.newArrayList;
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
		dataService.findAll(ViewMetaData.ENTITY_NAME).map(viewEntity -> viewEntity.getString(ViewMetaData.NAME))
				.forEach(this::registerRepository);
	}

	private void registerRepository(String viewName)
	{
		Repository viewRepository = viewRepositoryCollection.addViewRepository(viewName);
		dataService.addRepository(viewRepository);
	}

	@Override
	public Entity getViewEntity(String viewName, String masterEntityName)
	{
		Entity viewEntity = dataService.findOne(ViewMetaData.ENTITY_NAME,
				new QueryImpl().eq(ViewMetaData.NAME, viewName).and().eq(ViewMetaData.MASTER_ENTITY, masterEntityName));
		return viewEntity;
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
		Entity newAttributeMapping = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		Entity slaveEntity = createNewSlaveEntity(slaveEntityName, newAttributeMapping);

		Entity newViewEntity = new MapEntity(new ViewMetaData());
		newViewEntity.set(ViewMetaData.NAME, viewName);
		newViewEntity.set(ViewMetaData.MASTER_ENTITY, masterEntityName);
		newViewEntity.set(ViewMetaData.SLAVE_ENTITIES, newArrayList(slaveEntity));
		dataService.add(ViewMetaData.ENTITY_NAME, newViewEntity);

		registerRepository(viewName);
	}

	@Override
	public void addNewSlaveEntityToExistingView(Entity viewEntity, String slaveEntityName, String masterAttributeId,
			String slaveAttributeId)
	{
		Entity newAttributeMapping = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);
		Entity slaveEntity = createNewSlaveEntity(slaveEntityName, newAttributeMapping);

		DefaultEntity newViewEntity = new DefaultEntity(viewEntity.getEntityMetaData(), dataService, viewEntity);
		List<Entity> slaveEntities = newArrayList(viewEntity.getEntities(ViewMetaData.SLAVE_ENTITIES));
		slaveEntities.add(slaveEntity);

		newViewEntity.set(ViewMetaData.SLAVE_ENTITIES, slaveEntities);
		update(ViewMetaData.ENTITY_NAME, newViewEntity);
	}

	@Override
	public void addNewAttributeMappingToExistingSlave(String slaveEntityName, String masterAttributeId,
			String slaveAttributeId)
	{
		Entity existingSlaveEntity = getSlaveEntity(slaveEntityName);
		Entity newAttributeMapping = createNewAttributeMappingEntity(masterAttributeId, slaveAttributeId);

		DefaultEntity newSlaveEntity = new DefaultEntity(new SlaveEntityMetaData(), dataService, existingSlaveEntity);
		List<Entity> attributeMappingList = newArrayList(
				existingSlaveEntity.getEntities(SlaveEntityMetaData.JOINED_ATTRIBUTES));
		attributeMappingList.add(newAttributeMapping);
		newSlaveEntity.set(SlaveEntityMetaData.JOINED_ATTRIBUTES, attributeMappingList);
		update(SlaveEntityMetaData.ENTITY_NAME, newSlaveEntity);
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
		add(JoinedAttributeMetaData.ENTITY_NAME, newAttributeMapping);
		return newAttributeMapping;
	}

	private Entity createNewSlaveEntity(String slaveEntityName, Entity newAttributeMapping)
	{
		Entity slaveEntity = new MapEntity(new SlaveEntityMetaData());
		slaveEntity.set(SlaveEntityMetaData.SLAVE_ENTITY, slaveEntityName);
		slaveEntity.set(SlaveEntityMetaData.JOINED_ATTRIBUTES, newArrayList(newAttributeMapping));
		add(SlaveEntityMetaData.ENTITY_NAME, slaveEntity);
		return slaveEntity;
	}

	private void add(String entityName, Entity entity)
	{
		dataService.add(entityName, entity);
	}

	private void update(String entityName, Entity entity)
	{
		dataService.update(entityName, entity);
	}

}
