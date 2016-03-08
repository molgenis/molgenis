package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class GroupAuthority extends org.molgenis.data.support.AbstractEntity implements Authority
{
	private static final long serialVersionUID = -3440011188128914033L;

	public static final String ENTITY_NAME = "GroupAuthority";
	public static final String MOLGENISGROUP = "molgenisGroup";
	public static final String ROLE = "role";
	public static final String ID = "id";

	String id;
	MolgenisGroup molgenisGroup;
	String role;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public MolgenisGroup getMolgenisGroup()
	{
		return molgenisGroup;
	}

	public void setMolgenisGroup(MolgenisGroup molgenisGroup)
	{
		this.molgenisGroup = molgenisGroup;
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
		if (name.equals(MOLGENISGROUP)) return getMolgenisGroup();
		return null;
	}

	@Override
	public void set(Entity entity)
	{
		set(entity, true);
	}

	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
		if (entity.getString(ROLE) != null) this.setRole(entity.getString(ROLE));
		if (entity.getString(ID) != null) this.setId(entity.getString(ID));
		if (entity.getEntity(MOLGENISGROUP, org.molgenis.auth.MolgenisGroup.class) != null)
		{
			this.setMolgenisGroup(entity.getEntity(MOLGENISGROUP, org.molgenis.auth.MolgenisGroup.class));
		}
	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("GroupAuthority(");
		sb.append("role='" + getRole() + "' ");
		sb.append("id='" + getId() + "' ");
		sb.append("molgenisGroup='" + getMolgenisGroup() + "'");
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
		for (AttributeMetaData attr : new GroupAuthorityMetaData().getAttributes())
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
		if (MOLGENISGROUP.equals(attributeName))
		{
			org.molgenis.auth.MolgenisGroup e = new org.molgenis.auth.MolgenisGroup();
			e.set((Entity) value);
			this.setMolgenisGroup(e);
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
		return new GroupAuthorityMetaData();
	}
}
