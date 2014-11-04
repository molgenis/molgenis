package org.molgenis.data.examples;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractEntity;

/**
 * Created by mswertz on 09/05/14.
 */
public class User extends AbstractEntity implements Entity
{
	private String username;
	private boolean active;

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return null;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return null;
	}

	@Override
	public Object getIdValue()
	{
		return null;
	}

	@Override
	public String getLabelValue()
	{
		return null;
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return null;
	}

	@Override
	public Object get(String attributeName)
	{
		return null;
	}

	@Override
	public String getString(String attributeName)
	{
		return null;
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return null;
	}

	@Override
	public Long getLong(String attributeName)
	{
		return null;
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return null;
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return null;
	}

	@Override
	public Date getDate(String attributeName)
	{
		return null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return null;
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return null;
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return null;
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return null;
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return null;
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return null;
	}

	@Override
	public void set(String attributeName, Object value)
	{

	}

	@Override
	public void set(Entity values)
	{

	}

	@Override
	public void set(Entity entity, boolean strict)
	{

	}
}
