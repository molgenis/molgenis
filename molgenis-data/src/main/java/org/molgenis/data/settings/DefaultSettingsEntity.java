package org.molgenis.data.settings;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.EntityMetaData;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for application and plugin settings entities. Settings are read/written from/to data source.
 */
public abstract class DefaultSettingsEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final String entityName;

	@Autowired
	private DataService dataService;

	public DefaultSettingsEntity(String entityId)
	{
		this.entityName = SettingsEntityMeta.PACKAGE_NAME + '_' + entityId;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return RunAsSystemProxy.runAsSystem(() -> {
			return dataService.getEntityMetaData(entityName);
		});
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return getEntity().getAttributeNames();
	}

	@Override
	public Object getIdValue()
	{
		return getEntity().getIdValue();
	}

	@Override
	public String getLabelValue()
	{
		return getEntity().getLabelValue();
	}

	@Override
	public Object get(String attributeName)
	{
		return getEntity().get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return getEntity().getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return getEntity().getInt(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return getEntity().getLong(attributeName);
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return getEntity().getBoolean(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return getEntity().getDouble(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return getEntity().getDate(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return getEntity().getUtilDate(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return getEntity().getTimestamp(attributeName);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return getEntity().getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return getEntity().getEntity(attributeName, clazz);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return getEntity().getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return getEntity().getEntities(attributeName, clazz);
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return getEntity().getList(attributeName);
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return getEntity().getIntList(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		Entity entity = getEntity();
		entity.set(attributeName, value);
		updateEntity(entity);
	}

	@Override
	public void set(Entity values)
	{
		Entity entity = getEntity();
		entity.set(values);
		updateEntity(entity);
	}

	/**
	 * Adds a listener for this settings entity that fires on entity updates
	 * 
	 * @param settingsEntityListener
	 *            listener for this settings entity
	 */
	public void addListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.addEntityListener(entityName, new EntityListener()
			{
				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityMetaData().getSimpleName();
				}
			});
		});
	}

	/**
	 * Removes a listener for this settings entity that fires on entity updates
	 * 
	 * @param settingsEntityListener
	 *            listener for this settings entity
	 */
	public void removeListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.removeEntityListener(entityName, new EntityListener()
			{

				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityMetaData().getSimpleName();
				}
			});
		});
	}

	private Entity getEntity()
	{
		String id = getEntityMetaData().getSimpleName();
		return RunAsSystemProxy.runAsSystem(() -> {
			return dataService.findOne(entityName, id);
		});
	}

	private void updateEntity(Entity entity)
	{
		RunAsSystemProxy.runAsSystem(() -> {
			dataService.update(entityName, entity);
			return null;
		});
	}
}
