package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class UserAuthority extends org.molgenis.data.support.AbstractEntity
		implements org.molgenis.data.Entity, org.molgenis.auth.Authority
{
	private static final long serialVersionUID = -2301668625608942549L;

	public static final String ENTITY_NAME = "UserAuthority";
	public static final String MOLGENISUSER = "molgenisUser";
	public static final String ROLE = "role";
	public static final String ID = "id";

	String id;
	MolgenisUser molgenisUser;
	String role;

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

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	@Override
	public Object get(String name)
	{
		if (name.equals(ROLE)) return getRole();
		if (name.equals(ID)) return getId();
		if (name.equals(MOLGENISUSER)) return getMolgenisUser();
		return null;
	}

	@Override
	public void set(Entity entity)
	{
		set(entity, true);
	}

	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
		// set Role
		// query formal name, else lowercase name
		if (entity.getString(ROLE) != null) this.setRole(entity.getString(ROLE));
		if (entity.getString(ID) != null) this.setId(entity.getString(ID));
		if (entity.getEntity(MOLGENISUSER, org.molgenis.auth.MolgenisUser.class) != null)
		{
			this.setMolgenisUser(entity.getEntity(MOLGENISUSER, org.molgenis.auth.MolgenisUser.class));
		}
	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("UserAuthority(");
		sb.append("role='" + getRole() + "' ");
		sb.append("id='" + getId() + "' ");
		sb.append("molgenisUser='" + getMolgenisUser() + "'");
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
		for (AttributeMetaData attr : new UserAuthorityMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (ROLE.equals(attributeName))
		{
			this.setRole((String) value);
			return;
		}
		if (ID.equals(attributeName))
		{
			this.setId((String) value);
			return;
		}
		if (MOLGENISUSER.equals(attributeName))
		{
			org.molgenis.auth.MolgenisUser e = new org.molgenis.auth.MolgenisUser();
			e.set((Entity) value);
			this.setMolgenisUser(e);
			return;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		return result;
	}

	@Override
	public org.molgenis.data.EntityMetaData getEntityMetaData()
	{
		return new UserAuthorityMetaData();
	}
}
