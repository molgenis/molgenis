package org.molgenis.auth;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.util.Date;

import static org.molgenis.auth.MolgenisTokenMetaData.*;

public class MolgenisToken extends StaticEntity
{
	public MolgenisToken(Entity entity)
	{
		super(entity);
	}

	public MolgenisToken(EntityType entityType)
	{
		super(entityType);
	}

	public MolgenisToken(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public MolgenisUser getMolgenisUser()
	{
		return getEntity(MOLGENIS_USER, MolgenisUser.class);
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		set(MOLGENIS_USER, molgenisUser);
	}

	public String getToken()
	{
		return getString(TOKEN);
	}

	public void setToken(String token)
	{
		set(TOKEN, token);
	}

	public Date getExpirationDate()
	{
		return getUtilDate(EXPIRATIONDATE);
	}

	public void setExpirationDate(Date expirationDate)
	{
		set(EXPIRATIONDATE, expirationDate);
	}

	public Date getCreationDate()
	{
		return getUtilDate(CREATIONDATE);
	}

	public void setCreationDate(Date creationDate)
	{
		set(CREATIONDATE, creationDate);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}
}
