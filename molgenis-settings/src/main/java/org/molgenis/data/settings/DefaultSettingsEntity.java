package org.molgenis.data.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.listeners.EntityListener;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.settings.SettingsPackage.PACKAGE_SETTINGS;

/**
 * Base class for application and plugin settings entities. Settings are read/written from/to data source.
 * TODO: Bring this class up to date with 2.0, see http://www.molgenis.org/ticket/4787
 */
public abstract class DefaultSettingsEntity extends StaticEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final String entityId;
	private final String entityTypeId;

	@Autowired
	private DataService dataService;

	@Autowired
	private EntityListenersService entityListenersService;

	private transient Entity cachedEntity;

	public DefaultSettingsEntity(String entityId)
	{
		this.entityId = requireNonNull(entityId);
		this.entityTypeId = PACKAGE_SETTINGS + PACKAGE_SEPARATOR + entityId;
	}

	public EntityType getEntityType()
	{
		return RunAsSystemProxy.runAsSystem(() -> dataService.getEntityType(entityTypeId));
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
	public Instant getInstant(String attributeName)
	{
		return getEntity().getInstant(attributeName);
	}

	@Override
	public LocalDate getLocalDate(String attributeName)
	{
		return getEntity().getLocalDate(attributeName);
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
		cachedEntity = values;
	}

	/**
	 * Adds a listener for this settings entity that fires on entity updates
	 *
	 * @param settingsEntityListener listener for this settings entity
	 */
	public void addListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> entityListenersService.addEntityListener(entityTypeId, new EntityListener()
		{
			@Override
			public void postUpdate(Entity entity)
			{
				settingsEntityListener.postUpdate(entity);
			}

			@Override
			public Object getEntityId()
			{
				return getEntityType().getId();
			}
		}));
	}

	/**
	 * Removes a listener for this settings entity that fires on entity updates
	 *
	 * @param settingsEntityListener listener for this settings entity
	 */
	public void removeListener(SettingsEntityListener settingsEntityListener)
	{
		RunAsSystemProxy.runAsSystem(() -> entityListenersService.removeEntityListener(entityTypeId, new EntityListener()
		{

			@Override
			public void postUpdate(Entity entity)
			{
				settingsEntityListener.postUpdate(entity);
			}

			@Override
			public Object getEntityId()
			{
				return getEntityType().getId();
			}
		}));
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
			cachedEntity = RunAsSystemProxy.runAsSystem(() ->
			{
				Entity entity = dataService.findOneById(entityTypeId, entityId);

				// refresh cache on settings update
				entityListenersService.addEntityListener(entityTypeId, new EntityListener()
				{
					@Override
					public void postUpdate(Entity entity)
					{
						cachedEntity = entity;
					}

					@Override
					public Object getEntityId()
					{
						return entityId;
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
			dataService.update(entityTypeId, entity);
			ResourceBundle.clearCache();

			// cache refresh is handled via entity listener
			return null;
		});
	}
}
