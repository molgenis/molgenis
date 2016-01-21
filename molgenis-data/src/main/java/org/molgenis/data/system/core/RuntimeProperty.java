package org.molgenis.data.system.core;

import com.google.auto.value.AutoValue;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.auth.RuntimePropertyMetaData;

import java.util.LinkedHashSet;
import java.util.Set;

@AutoValue
public class RuntimeProperty  extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity{

    public static final String ENTITY_NAME = "RuntimeProperty";

    public static final String NAME = "Name";
    public static final String VALUE = "Value";
    public static final String ID = "id";
    String id;
    String name;
    String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Object get(String name)
    {
        name = name.toLowerCase();
        if (name.equals(ID))
            return getId();
        if (name.equals(NAME))
            return getName();
        if (name.equals(VALUE))
            return getValue();
        return null;
    }

    @Override
    public void set(Entity entity)
    {
        set(entity, true);
    }

    public void set(org.molgenis.data.Entity entity, boolean strict)
    {
        if(entity.getString(ID) != null) this.setId(entity.getString(ID));
        if(entity.getString(NAME) != null) this.setName(entity.getString(NAME));
        if(entity.getString(VALUE) != null) this.setValue(entity.getString(VALUE));
    }

    @Override
    public String toString()
    {
        return this.toString(false);
    }

    public String toString(boolean verbose)
    {
        StringBuilder sb = new StringBuilder("RuntimeProperty(");
        sb.append("id='" + getId()+"' ");
        sb.append("name='" + getName()+"' ");
        sb.append("value='" + getValue()+"'");
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
        for (AttributeMetaData attr : new RuntimePropertyMetaData().getAttributes())
        {
            attributeNames.add(attr.getName());
        }

        return attributeNames;
    }

    @Override
    public void set(String attributeName, Object value)
    {
        if(ID.equals(attributeName)) {
            this.setId((String)value);
            return;
        }
        if(NAME.equals(attributeName)) {
            this.setName((String)value);
            return;
        }
        if(VALUE.equals(attributeName)) {
            this.setValue((String)value);
            return;
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RuntimeProperty other = (RuntimeProperty) obj;
        if (name == null)
        {
            if (other.name != null) return false;
        }
        else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public org.molgenis.data.EntityMetaData getEntityMetaData()
    {
        return new RuntimePropertyMetaData();
    }
}
