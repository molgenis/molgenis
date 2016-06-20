package org.molgenis.auth;

import static org.molgenis.auth.MolgenisTokenMetaData.CREATIONDATE;
import static org.molgenis.auth.MolgenisTokenMetaData.DESCRIPTION;
import static org.molgenis.auth.MolgenisTokenMetaData.EXPIRATIONDATE;
import static org.molgenis.auth.MolgenisTokenMetaData.ID;
import static org.molgenis.auth.MolgenisTokenMetaData.MOLGENIS_USER;
import static org.molgenis.auth.MolgenisTokenMetaData.TOKEN;

import java.util.Date;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class MolgenisToken extends StaticEntity
{
	public MolgenisToken(Entity entity)
	{
		super(entity);
	}

	public MolgenisToken(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public MolgenisToken(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
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
