package org.molgenis.data.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ResourceBundle;

import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;

/**
 * Base class for application and plugin settings entities. Settings are read/written from/to data source.
 */
public abstract class DefaultSettingsEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final String entityName;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityListenersService entityListenersService;

	private transient Entity cachedEntity;

	public DefaultSettingsEntity(String entityId)
	{
		this.entityName = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + entityId;
	}

	public EntityType getEntityType()
	{
		return RunAsSystemProxy.runAsSystem(() ->
		{
			return dataService.getEntityType(entityName);
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
	public void setIdValue(Object id)
	{
		getEntity().setIdValue(id);
	}

	@Override
	public Object getLabelValue()
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
	 * @param settingsEntityListener listener for this settings entity
	 */
	public void addListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() ->
		{
			entityListenersService.addEntityListener(entityName, new EntityListener()
			{
				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityType().getSimpleName();
				}
			});
		});
	}

	/**
	 * Removes a listener for this settings entity that fires on entity updates
	 *
	 * @param settingsEntityListener listener for this settings entity
	 */
	public void removeListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() ->
		{
			entityListenersService.removeEntityListener(entityName, new EntityListener()
			{

				@Override
				public void postUpdate(Entity entity)
				{
					settingsEntityListener.postUpdate(entity);
				}

				@Override
				public Object getEntityId()
				{
					return getEntityType().getSimpleName();
				}
			});
		});
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Entity)) return false;
		return EntityUtils.equals(this, (Entity) o);
	}

	@Override
	public int hashCode()
	{
		return EntityUtils.hashCode(this);
	}

	private Entity getEntity()
	{
		if (cachedEntity == null)
		{
			String id = getEntityType().getSimpleName();
			cachedEntity = RunAsSystemProxy.runAsSystem(() ->
			{
				Entity entity = dataService.findOneById(entityName, id);

				// refresh cache on settings update
				entityListenersService.addEntityListener(entityName, new EntityListener()
				{
					@Override
					public void postUpdate(Entity entity)
					{
						cachedEntity = entity;
					}

					@Override
					public Object getEntityId()
					{
						return id;
					}
				});
				return entity;
			});

		}
		return cachedEntity;
	}

	private void updateEntity(Entity entity)
	{
		RunAsSystemProxy.runAsSystem(() ->
		{
			dataService.update(entityName, entity);
			ResourceBundle.clearCache();

			// cache refresh is handled via entity listener
			return null;
		});
	}
}
