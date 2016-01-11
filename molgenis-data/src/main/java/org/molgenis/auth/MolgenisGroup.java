package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class MolgenisGroup  extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{
    public static final String ENTITY_NAME = "MolgenisGroup";
    public static final String NAME = "name";
    public static final String ID = "id";
    public static final String ACTIVE = "active";

    String id;
    String name;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    boolean active;


    @Override
    public Object get(String name)
    {
        if (name.equals(ID))
            return getId();
        if (name.equals(NAME))
            return getName();
        if (name.equals(ACTIVE))
            return isActive();
        return null;
    }

    @Override
    public void set(Entity entity)
    {
        set(entity, true);
    }

    public void set(org.molgenis.data.Entity entity, boolean strict)
    {
        //set Id
        // query formal name, else lowercase name
        if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(entity.getString("id") != null) this.setId(entity.getString("id"));
        else if(strict) this.setId(entity.getString("id")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisgroup_id") != null) this.setId(entity.getString("molgenisgroup_id"));
        else if( entity.getString("MolgenisGroup_id") != null) this.setId(entity.getString("MolgenisGroup_id"));
        //set Name
        // query formal name, else lowercase name
        if(entity.getString("name") != null) this.setName(entity.getString("name"));
        else if(entity.getString("name") != null) this.setName(entity.getString("name"));
        else if(strict) this.setName(entity.getString("name")); // setting null is not an option due to function overloading
        if( entity.getString("molgenisgroup_name") != null) this.setName(entity.getString("molgenisgroup_name"));
        else if( entity.getString("MolgenisGroup_name") != null) this.setName(entity.getString("MolgenisGroup_name"));
        //set Active
        // query formal name, else lowercase name
        if(entity.getBoolean("active") != null) this.setActive(entity.getBoolean("active"));
        else if(entity.getBoolean("active") != null) this.setActive(entity.getBoolean("active"));
        else if(strict) this.setActive(entity.getBoolean("active")); // setting null is not an option due to function overloading
        if( entity.getBoolean("molgenisgroup_active") != null) this.setActive(entity.getBoolean("molgenisgroup_active"));
        else if( entity.getBoolean("MolgenisGroup_active") != null) this.setActive(entity.getBoolean("MolgenisGroup_active"));
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("MolgenisGroup(");
        sb.append("id='" + getId()+"' ");
        sb.append("name='" + getName()+"' ");
        sb.append("active='" + isActive()+"'");
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
        for (AttributeMetaData attr : new MolgenisGroupMetaData().getAttributes())
        {
            attributeNames.add(attr.getName());
        }

        return attributeNames;
    }

    @Override
    public void set(String attributeName, Object value)
    {
        if("id".equalsIgnoreCase(attributeName)) {
            this.setId((String)value);
            return;
        }
        if("name".equalsIgnoreCase(attributeName)) {
            this.setName((String)value);
            return;
        }
        if("active".equalsIgnoreCase(attributeName)) {
            this.setActive((Boolean)value);
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
        return new MolgenisGroupMetaData();
    }
}
