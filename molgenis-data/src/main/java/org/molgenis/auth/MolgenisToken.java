package org.molgenis.auth;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@AutoValue
public class MolgenisToken extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity
{
	private static final long serialVersionUID = -2596871010814579374L;

	public static final String ENTITY_NAME = "MolgenisToken";
	public static final String TOKEN = "token";
	public static final String ID = "id";
	public static final String MOLGENIS_USER = "molgenisUser";
	public static final String EXPIRATIONDATE = "expirationDate";
	public static final String CREATIONDATE = "creationDate";
	public static final String DESCRIPTION = "description";

	String id;
	MolgenisUser molgenisUser;
	String token;
	Date expirationDate;
	Date creationDate;
	String description;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public MolgenisUser getMolgenisUser()
	{
		return molgenisUser;
	}

	public void setMolgenisUser(MolgenisUser molgenisUser)
	{
		this.molgenisUser = molgenisUser;
	}

	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public Date getExpirationDate()
	{
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate)
	{
		this.expirationDate = expirationDate;
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public Object get(String name)
	{
		if (name.equals(ID)) return getId();
		if (name.equals(MOLGENIS_USER)) return getMolgenisUser();
		if (name.equals(TOKEN)) return getToken();
		if (name.equals(EXPIRATIONDATE)) return getExpirationDate();
		if (name.equals(CREATIONDATE)) return getCreationDate();
		if (name.equals(DESCRIPTION)) return getDescription();
		return null;
	}

	@Override
	public void set(Entity entity)
	{
		set(entity, true);
	}

	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
		// set Id
		// query formal name, else lowercase name
		if (entity.getString(ID) != null) this.setId(entity.getString(ID));
		if (entity.getString("MolgenisToken_id") != null) this.setId(entity.getString("MolgenisToken_id"));
		if (entity.getEntity(MOLGENIS_USER, org.molgenis.auth.MolgenisUser.class) != null)
		{
			this.setMolgenisUser(entity.getEntity(MOLGENIS_USER, org.molgenis.auth.MolgenisUser.class));
		}
		if (entity.getEntity("MolgenisToken_molgenisUser", org.molgenis.auth.MolgenisUser.class) != null)
		{
			this.setMolgenisUser(entity.getEntity("MolgenisToken_molgenisUser", org.molgenis.auth.MolgenisUser.class));
		}
		if (entity.getString(TOKEN) != null) this.setToken(entity.getString(TOKEN));
		if (entity.getString("MolgenisToken_token") != null) this.setToken(entity.getString("MolgenisToken_token"));
		if (entity.getTimestamp(EXPIRATIONDATE) != null) this.setExpirationDate(entity.getTimestamp(EXPIRATIONDATE));
		if (entity.getTimestamp("MolgenisToken_expirationDate") != null)
			this.setExpirationDate(entity.getTimestamp("MolgenisToken_expirationDate"));
		if (entity.getTimestamp(CREATIONDATE) != null) this.setCreationDate(entity.getTimestamp(CREATIONDATE));
		if (entity.getTimestamp("MolgenisToken_creationDate") != null)
			this.setCreationDate(entity.getTimestamp("MolgenisToken_creationDate"));
		if (entity.getString(DESCRIPTION) != null) this.setDescription(entity.getString(DESCRIPTION));
		if (entity.getString("MolgenisToken_description") != null)
			this.setDescription(entity.getString("MolgenisToken_description"));
	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("MolgenisToken(");
		sb.append("id='" + getId() + "' ");
		sb.append("molgenisUser='" + getMolgenisUser() + "' ");
		sb.append("token='" + getToken() + "' ");
		sb.append("expirationDate='" + (getExpirationDate() == null ? ""
				: new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US)
						.format(getExpirationDate()))
				+ "' ");
		sb.append("creationDate='" + (getCreationDate() == null ? ""
				: new java.text.SimpleDateFormat("MMMM d, yyyy, HH:mm:ss", java.util.Locale.US)
						.format(getCreationDate()))
				+ "' ");
		sb.append("description='" + getDescription() + "'");
		sb.append(");");
		return sb.toString();
	}

	@Override
	public String getIdValue()
	{
		return getId();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		Set<String> attributeNames = new LinkedHashSet<String>();
		for (AttributeMetaData attr : new MolgenisTokenMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (ID.equalsIgnoreCase(attributeName))
		{
			this.setId((String) value);
			return;
		}
		if (MOLGENIS_USER.equalsIgnoreCase(attributeName))
		{
			org.molgenis.auth.MolgenisUser e = new org.molgenis.auth.MolgenisUser();
			e.set((Entity) value);
			this.setMolgenisUser(e);
			return;
		}
		if (TOKEN.equalsIgnoreCase(attributeName))
		{
			this.setToken((String) value);
			return;
		}
		if (EXPIRATIONDATE.equalsIgnoreCase(attributeName))
		{
			this.setExpirationDate((java.util.Date) value);
			return;
		}
		if (CREATIONDATE.equalsIgnoreCase(attributeName))
		{
			this.setCreationDate((java.util.Date) value);
			return;
		}
		if (DESCRIPTION.equalsIgnoreCase(attributeName))
		{
			this.setDescription((String) value);
			return;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MolgenisToken other = (MolgenisToken) obj;
		if (token == null)
		{
			if (other.token != null) return false;
		}
		else if (!token.equals(other.token)) return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public org.molgenis.data.EntityMetaData getEntityMetaData()
	{
		return new MolgenisTokenMetaData();
	}
}
